package com.fumbbl.ffb.server.admin;

import com.eclipsesource.json.JsonValue;
import com.fumbbl.ffb.json.UtilJson;
import com.fumbbl.ffb.server.FantasyFootballServer;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerProperty;
import com.fumbbl.ffb.util.ArrayTool;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 *
 * @author Kalimar
 */
public class UtilBackup {

	private static File findBackupFile(FantasyFootballServer pServer, long pGameId) {
		if ((pServer == null) || (pGameId <= 0)) {
			return null;
		}
		String gameIdString = Long.toString(pGameId);
		String pathName = pServer.getProperty(IServerProperty.BACKUP_DIR) + "/" +
			calculateFolderPathForGame(pServer, gameIdString);
		return new File(pathName);
	}

	public static String calculateFolderPathForGame(FantasyFootballServer pServer, String gameIdString) {
		StringBuilder pathName = new StringBuilder();
		int index = 0;
		for (int i = 7; i > 3; i--) {
			if (gameIdString.length() < i) {
				pathName.append("0");
			} else {
				pathName.append(gameIdString.charAt(index++));
			}
			pathName.append("/");
		}
		pathName.append(gameIdString).append(".").append(pServer.getProperty(IServerProperty.BACKUP_EXTENSION));
		return pathName.toString();
	}

	public static boolean save(GameState pGameState) {
		if (pGameState == null) {
			return false;
		}
		File backupFile = findBackupFile(pGameState.getServer(), pGameState.getId());
		//noinspection ResultOfMethodCallIgnored
		backupFile.getParentFile().mkdirs();
		try (BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(backupFile.toPath()))) {
			out.write(UtilJson.gzip(pGameState.toJsonValue()));
		} catch (IOException pIoException) {
			pGameState.getServer().getDebugLog().log(pGameState.getId(), pIoException);
			return false;
		}
		// nothing to be done here
		return true;
	}

	public static byte[] loadAsGzip(FantasyFootballServer pServer, long pGameId) {
		if ((pServer == null) || (pGameId <= 0)) {
			return null;
		}
		File backupFile = findBackupFile(pServer, pGameId);
		if (!backupFile.exists()) {
			return null;
		}
		byte[] gzippedJson = new byte[(int) backupFile.length()];
		try (DataInputStream in = new DataInputStream(Files.newInputStream(backupFile.toPath()))) {
			in.readFully(gzippedJson);
		} catch (IOException pIoException) {
			pServer.getDebugLog().log(pGameId, pIoException);
			return null;
		}
		// nothing to be done here
		return gzippedJson;
	}

	public static GameState load(FantasyFootballServer pServer, long pGameId) {
		byte[] gzippedJson = loadAsGzip(pServer, pGameId);
		if (ArrayTool.isProvided(gzippedJson)) {
			JsonValue jsonValue = null;
			try {
				jsonValue = UtilJson.gunzip(gzippedJson);
			} catch (IOException pIoException) {
				pServer.getDebugLog().log(pGameId, pIoException);
			}
			GameState gameState = null;
			if (jsonValue != null) {
				gameState = new GameState(pServer);
				gameState.initFrom(gameState.getGame().getRules(), jsonValue);
				return gameState;
			}

		}
		return null;
	}

}
