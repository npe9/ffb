package com.fumbbl.ffb.server.admin;

import com.fumbbl.ffb.CommonProperty;
import com.fumbbl.ffb.FantasyFootballException;
import com.fumbbl.ffb.GameStatus;
import com.fumbbl.ffb.IClientPropertyValue;
import com.fumbbl.ffb.PasswordChallenge;
import com.fumbbl.ffb.factory.GameStatusFactory;
import com.fumbbl.ffb.model.Game;
import com.fumbbl.ffb.server.FantasyFootballServer;
import com.fumbbl.ffb.server.GameState;
import com.fumbbl.ffb.server.IServerProperty;
import com.fumbbl.ffb.server.db.DbStatementId;
import com.fumbbl.ffb.server.db.query.DbAdminListByIdQuery;
import com.fumbbl.ffb.server.db.query.DbAdminListByStatusQuery;
import com.fumbbl.ffb.server.db.query.DbTestGameListQuery;
import com.fumbbl.ffb.server.db.query.DbUserSettingsQuery;
import com.fumbbl.ffb.server.net.ServerCommunication;
import com.fumbbl.ffb.server.net.commands.InternalServerCommandCloseGame;
import com.fumbbl.ffb.server.net.commands.InternalServerCommandDeleteGame;
import com.fumbbl.ffb.server.net.commands.InternalServerCommandScheduleGame;
import com.fumbbl.ffb.server.net.commands.InternalServerCommandUploadGame;
import com.fumbbl.ffb.server.request.ServerRequestSaveReplay;
import com.fumbbl.ffb.server.util.rng.Fortuna;
import com.fumbbl.ffb.util.ArrayTool;
import com.fumbbl.ffb.util.DateTool;
import com.fumbbl.ffb.util.StringTool;
import com.fumbbl.ffb.xml.UtilXml;
import org.eclipse.jetty.websocket.api.Session;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.sax.TransformerHandler;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kalimar
 */
public class AdminServlet extends HttpServlet {

	public static final String BACKUP = "backup";
	public static final String BLOCK = "block";
	public static final String CACHE = "cache";
	public static final String CHALLENGE = "challenge";
	public static final String CLOSE = "close";
	public static final String CONCEDE = "concede";
	public static final String DELETE = "delete";
	public static final String LIST = "list";
	public static final String LOGLEVEL = "loglevel";
	public static final String MESSAGE = "message";
	public static final String REFRESH = "refresh";
	public static final String SCHEDULE = "schedule";
	public static final String SHUTDOWN = "shutdown";
	public static final String STATS = "stats";
	public static final String UNBLOCK = "unblock";
	public static final String UPLOAD = "upload";
	public static final String FORCE_LOG = "forcelog";
	public static final String PORTRAIT = "portrait";
	public static final String PURGE_TEST = "purgetest";

	private static final String _STATUS_OK = "ok";
	private static final String _STATUS_FAIL = "fail";

	private static final String _PARAMETER_COACH = "coach";
	private static final String _PARAMETER_RESPONSE = "response";
	private static final String _PARAMETER_GAME_ID = "gameId";
	private static final String _PARAMETER_TEAM_ID = "teamId";
	private static final String _PARAMETER_STATUS = "status";
	private static final String _PARAMETER_MESSSAGE = "message";
	private static final String _PARAMETER_TEAM_HOME_ID = "teamHomeId";
	private static final String _PARAMETER_TEAM_AWAY_ID = "teamAwayId";
	private static final String _PARAMETER_VALUE = "value";
	private static final String _PARAMETER_LIMIT = "limit";
	private static final String _PARAMETER_PERFORM = "perform";

	private static final String _XML_TAG_ADMIN = "admin";
	private static final String _XML_TAG_BACKUP = "challenge";
	private static final String _XML_TAG_CHALLENGE = "challenge";
	private static final String _XML_TAG_CONCEDE = "concede";
	private static final String _XML_TAG_LIST = "list";
	private static final String _XML_TAG_SHUTDOWN = "shutdown";
	private static final String _XML_TAG_CLOSE = "close";
	private static final String _XML_TAG_UPLOAD = "upload";
	private static final String _XML_TAG_DELETE = "delete";
	private static final String _XML_TAG_MESSAGE = "message";
	private static final String _XML_TAG_ERROR = "error";
	private static final String _XML_TAG_REFRESH = "refresh";
	private static final String _XML_TAG_SCHEDULE = "schedule";
	private static final String _XML_TAG_GAME_ID = "gameId";
	private static final String _XML_TAG_BLOCK = "block";
	private static final String _XML_TAG_UNBLOCK = "unblock";
	private static final String _XML_TAG_STATUS = "status";
	private static final String _XML_TAG_LOGLEVEL = "loglevel";
	private static final String _XML_TAG_FORCE_LOG = "forcelog";
	private static final String _XML_TAG_DELETED = "deleted";

	private static final String _XML_ATTRIBUTE_INITIATED = "initiated";
	private static final String _XML_ATTRIBUTE_GAME_ID = "gameId";
	private static final String _XML_ATTRIBUTE_TEAM_ID = "teamId";
	private static final String _XML_ATTRIBUTE_TEAM_HOME_ID = "teamHomeId";
	private static final String _XML_ATTRIBUTE_TEAM_AWAY_ID = "teamAwayId";
	private static final String _XML_ATTRIBUTE_GAME_STATUS = "gameStatus";
	private static final String _XML_ATTRIBUTE_SIZE = "size";
	private static final String _XML_ATTRIBUTE_VALUE = "value";

	private static final DateFormat _TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); // 2001-07-04T12:08:56.235

	private final FantasyFootballServer fServer;
	private String fLastChallenge;

	public AdminServlet(FantasyFootballServer pServer) {
		fServer = pServer;
	}

	private static int compare(GameState o1, GameState o2) {
		return Long.compare(o1.getId(), o2.getId());
	}

	public FantasyFootballServer getServer() {
		return fServer;
	}

	@Override
	protected void doGet(HttpServletRequest pRequest, HttpServletResponse pResponse)
		throws IOException {

		pResponse.setContentType("text/xml; charset=UTF-8");

		boolean isOk;
		TransformerHandler handler = UtilXml.createTransformerHandler(pResponse.getWriter(), true);

		try {
			handler.startDocument();
		} catch (SAXException pSaxException) {
			throw new FantasyFootballException(pSaxException);
		}

		UtilXml.startElement(handler, _XML_TAG_ADMIN);

		String command = pRequest.getPathInfo();
		if ((command != null) && (command.length() > 1) && command.startsWith("/")) {
			command = command.substring(1);
		}
		Map<String, String[]> parameters = pRequest.getParameterMap();

		if (CHALLENGE.equals(command)) {
			isOk = handleChallenge(handler);
		} else if (CACHE.equals(command)) {
			isOk = handleCache(handler);
		} else {
			isOk = checkResponse(ArrayTool.firstElement(parameters.get(_PARAMETER_RESPONSE)));
			if (isOk) {
				if (SHUTDOWN.equals(command)) {
					isOk = handleShutdown(handler);
				} else if (LIST.equals(command)) {
					isOk = handleList(handler, parameters);
				} else if (BACKUP.equals(command)) {
					isOk = handleBackup(handler, parameters);
				} else if (BLOCK.equals(command)) {
					isOk = handleBlock(handler, true);
				} else if (UNBLOCK.equals(command)) {
					isOk = handleBlock(handler, false);
				} else if (LOGLEVEL.equals(command)) {
					isOk = handleLogLevel(handler, parameters);
				} else if (CLOSE.equals(command)) {
					isOk = handleClose(handler, parameters);
				} else if (CONCEDE.equals(command)) {
					isOk = handleConcede(handler, parameters);
				} else if (UPLOAD.equals(command)) {
					isOk = handleUpload(handler, parameters);
				} else if (DELETE.equals(command)) {
					isOk = handleDelete(handler, parameters);
				} else if (MESSAGE.equals(command)) {
					isOk = handleMessage(handler, parameters);
				} else if (REFRESH.equals(command)) {
					isOk = handleRefresh(handler);
				} else if (SCHEDULE.equals(command)) {
					isOk = handleSchedule(handler, parameters);
				} else if (STATS.equals(command)) {
					isOk = handleStats(handler);
				} else if (FORCE_LOG.equals(command)) {
					isOk = handleForceLog(handler, parameters);
				} else if (PORTRAIT.equals(command)) {
					isOk = handlePortrait(handler, parameters);
				} else if (PURGE_TEST.equals(command)) {
					isOk = handlePurge(handler, parameters);
				} else {
					isOk = false;
				}
			}
		}

		UtilXml.addValueElement(handler, _XML_TAG_STATUS, isOk ? _STATUS_OK : _STATUS_FAIL);

		UtilXml.endElement(handler, _XML_TAG_ADMIN);

		try {
			handler.endDocument();
		} catch (SAXException pSaxException) {
			throw new FantasyFootballException(pSaxException);
		}

	}

	private boolean handleChallenge(TransformerHandler pHandler) {
		boolean isOk = true;
		String challenge = fServer.getProperty(IServerProperty.ADMIN_SALT) +
			System.currentTimeMillis();
		try {
			fLastChallenge = PasswordChallenge.toHexString(PasswordChallenge.md5Encode(challenge.getBytes()));
		} catch (NoSuchAlgorithmException pE) {
			fLastChallenge = null;
		}
		if (fLastChallenge != null) {
			UtilXml.addValueElement(pHandler, _XML_TAG_CHALLENGE, fLastChallenge);
		} else {
			isOk = false;
		}
		return isOk;
	}

	private boolean handleSchedule(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String teamHomeId = ArrayTool.firstElement(pParameters.get(_PARAMETER_TEAM_HOME_ID));
		if (!StringTool.isProvided(teamHomeId) || "0".equals(teamHomeId)) {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid parameter " + _PARAMETER_TEAM_HOME_ID);
			return false;
		}
		String teamAwayId = ArrayTool.firstElement(pParameters.get(_PARAMETER_TEAM_AWAY_ID));
		if (!StringTool.isProvided(teamAwayId) || "0".equals(teamAwayId)) {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid parameter " + _PARAMETER_TEAM_AWAY_ID);
			return false;
		}
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_TEAM_HOME_ID, teamHomeId);
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_TEAM_AWAY_ID, teamAwayId);
		UtilXml.addEmptyElement(pHandler, _XML_TAG_SCHEDULE, attributes);
		if (getServer().isBlockingNewGames()) {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "No new games allowed");
			return false;
		}
		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicLong gameIdHolder = new AtomicLong();
		InternalServerCommandScheduleGame scheduleCommand = new InternalServerCommandScheduleGame(teamHomeId, teamAwayId);
		scheduleCommand.setGameIdListener(pGameId -> {
			gameIdHolder.set(pGameId);
			latch.countDown();
		});
		getServer().getCommunication().handleCommand(scheduleCommand);
		try {
			latch.await();
		} catch (InterruptedException e) {
			// simply continue
		}
		UtilXml.addValueElement(pHandler, _XML_TAG_GAME_ID, gameIdHolder.get());
		return true;
	}

	private boolean handleClose(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String gameIdString = ArrayTool.firstElement(pParameters.get(_PARAMETER_GAME_ID));
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_GAME_ID, gameIdString);
		UtilXml.addEmptyElement(pHandler, _XML_TAG_CLOSE, attributes);
		long gameId = parseGameId(gameIdString);
		if (gameId > 0) {
			getServer().getCommunication().handleCommand(new InternalServerCommandCloseGame(gameId));
			return true;
		} else {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid or missing gameId parameter");
			return false;
		}
	}

	private boolean handleForceLog(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String gameIdString = ArrayTool.firstElement(pParameters.get(_PARAMETER_GAME_ID));
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_GAME_ID, gameIdString);
		UtilXml.addEmptyElement(pHandler, _XML_TAG_FORCE_LOG, attributes);
		long gameId = parseGameId(gameIdString);
		if (gameId > 0) {
			getServer().getDebugLog().forceLog(gameId);
			return true;
		} else {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid or missing gameId parameter");
			return false;
		}
	}

	private boolean handleLogLevel(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String valueString = ArrayTool.firstElement(pParameters.get(_PARAMETER_VALUE));
		int logLevel = StringTool.isProvided(valueString) ? Integer.parseInt(valueString) : 0;
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_VALUE, valueString);
		UtilXml.addEmptyElement(pHandler, _XML_TAG_LOGLEVEL, attributes);
		getServer().getDebugLog().setLogLevel(logLevel);
		return true;
	}

	private boolean handleBlock(TransformerHandler pHandler, boolean pBlockingNewGames) {
		if (pBlockingNewGames) {
			getServer().setBlockingNewGames(true);
			UtilXml.addEmptyElement(pHandler, _XML_TAG_BLOCK);
		} else {
			getServer().setBlockingNewGames(false);
			UtilXml.addEmptyElement(pHandler, _XML_TAG_UNBLOCK);
		}
		return true;
	}

	private long parseGameId(String pGameStateId) {
		if (StringTool.isProvided(pGameStateId)) {
			try {
				return Long.parseLong(pGameStateId);
			} catch (NumberFormatException pNfe) {
				// continue and return 0
			}
		}
		return 0;
	}

	private boolean handleDelete(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String gameIdString = ArrayTool.firstElement(pParameters.get(_PARAMETER_GAME_ID));
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_GAME_ID, gameIdString);
		UtilXml.addEmptyElement(pHandler, _XML_TAG_DELETE, attributes);
		long gameId = parseGameId(gameIdString);
		if (gameId > 0) {
			getServer().getCommunication().handleCommand(new InternalServerCommandCloseGame(gameId));
			getServer().getCommunication().handleCommand(new InternalServerCommandDeleteGame(gameId, true));
			return true;
		} else {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid or missing gameId parameter");
			return false;
		}
	}

	private boolean handleMessage(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String message = ArrayTool.firstElement(pParameters.get(_PARAMETER_MESSSAGE));
		if (StringTool.isProvided(message)) {
			UtilXml.addValueElement(pHandler, _XML_TAG_MESSAGE, StringTool.print(message));
			getServer().getCommunication().sendAdminMessage(new String[]{message});
			return true;
		} else {
			UtilXml.addEmptyElement(pHandler, _XML_TAG_MESSAGE);
			return false;
		}
	}

	private boolean handleUpload(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String gameIdString = ArrayTool.firstElement(pParameters.get(_PARAMETER_GAME_ID));
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_GAME_ID, gameIdString);
		UtilXml.addEmptyElement(pHandler, _XML_TAG_UPLOAD, attributes);
		long gameId = parseGameId(gameIdString);
		if (gameId > 0) {
			getServer().getCommunication().handleCommand(new InternalServerCommandUploadGame(gameId));
			return true;
		} else {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid or missing gameId parameter");
			return false;
		}
	}

	private boolean handleBackup(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String gameIdString = ArrayTool.firstElement(pParameters.get(_PARAMETER_GAME_ID));
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_GAME_ID, gameIdString);
		UtilXml.addEmptyElement(pHandler, _XML_TAG_BACKUP, attributes);
		long gameId = parseGameId(gameIdString);
		if (gameId > 0) {
			getServer().getRequestProcessor().add(new ServerRequestSaveReplay(gameId));
			return true;
		} else {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid or missing gameId parameter");
			return false;
		}
	}

	private boolean handleConcede(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		String teamId = ArrayTool.firstElement(pParameters.get(_PARAMETER_TEAM_ID));
		if (!StringTool.isProvided(teamId) || "0".equals(teamId)) {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid or missing teamId parameter");
			return false;
		}
		String gameIdString = ArrayTool.firstElement(pParameters.get(_PARAMETER_GAME_ID));
		long gameId = parseGameId(gameIdString);
		if (gameId > 0) {
			AttributesImpl attributes = new AttributesImpl();
			UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_GAME_ID, gameIdString);
			UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_TEAM_ID, teamId);
			UtilXml.addEmptyElement(pHandler, _XML_TAG_CONCEDE, attributes);
			getServer().getCommunication().handleCommand(new InternalServerCommandUploadGame(gameId, teamId));
			return true;
		} else {
			UtilXml.addValueElement(pHandler, _XML_TAG_ERROR, "Invalid or missing gameId parameter");
			return false;
		}
	}

	private boolean handleList(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		boolean isOk = true;
		AttributesImpl attributes = new AttributesImpl();
		GameStatus status = null;
		String statusParameter = ArrayTool.firstElement(pParameters.get(_PARAMETER_STATUS));
		if (StringTool.isProvided(statusParameter)) {
			UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_GAME_STATUS, statusParameter);
			status = new GameStatusFactory().forName(statusParameter);
		}
		long gameId = 0;
		String gameIdParameter = ArrayTool.firstElement(pParameters.get(_PARAMETER_GAME_ID));
		if (StringTool.isProvided(gameIdParameter)) {
			UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_GAME_ID, gameIdParameter);
			try {
				gameId = Long.parseLong(gameIdParameter);
			} catch (NumberFormatException ignored) {
			}
		}
		AdminList adminList = new AdminList();
		if (status != null) {
			DbAdminListByStatusQuery listQuery = (DbAdminListByStatusQuery) getServer().getDbQueryFactory()
				.getStatement(DbStatementId.ADMIN_LIST_BY_STATUS_QUERY);
			listQuery.execute(adminList, status);
		}
		if (gameId > 0) {
			DbAdminListByIdQuery listQuery = (DbAdminListByIdQuery) getServer().getDbQueryFactory()
				.getStatement(DbStatementId.ADMIN_LIST_BY_ID_QUERY);
			listQuery.execute(adminList, gameId);
		}
		addGames(pHandler, attributes, adminList);
		return isOk;
	}

	private boolean handlePortrait(TransformerHandler handler, Map<String, String[]> pParameters) {
		String coach = ArrayTool.firstElement(pParameters.get(_PARAMETER_COACH));

		DbUserSettingsQuery listQuery = (DbUserSettingsQuery) getServer().getDbQueryFactory()
			.getStatement(DbStatementId.USER_SETTINGS_QUERY);
		listQuery.execute(coach);
		boolean isPortrait = IClientPropertyValue.SETTING_PITCH_PORTRAIT.equals(listQuery.getSettingValue(CommonProperty.SETTING_PITCH_ORIENTATION));
		UtilXml.addValueElement(handler, "isPortrait", isPortrait);

		return true;
	}

	private boolean handleCache(TransformerHandler handler) {
		boolean isOk = true;
		GameState[] gameStates = getServer().getGameCache().allGameStates();
		Arrays.sort(gameStates, AdminServlet::compare);
		AttributesImpl cacheAttributes = new AttributesImpl();
		UtilXml.addAttribute(cacheAttributes, "size", gameStates.length);
		int activeGames = 0;
		for (GameState gameState : gameStates) {
			if (gameState.getStatus() == GameStatus.ACTIVE) {
				activeGames++;
			}
		}
		UtilXml.addAttribute(cacheAttributes, "activeGames", activeGames);
		UtilXml.startElement(handler, "cache", cacheAttributes);
		for (GameState gameState : gameStates) {
			Game game = gameState.getGame();
			AttributesImpl gameAttributes = new AttributesImpl();
			UtilXml.addAttribute(gameAttributes, "id", gameState.getId());
			UtilXml.addAttribute(gameAttributes, "name", getServer().getGameCache().getGameName(gameState.getId()));
			UtilXml.startElement(handler, "game", gameAttributes);
			if (gameState.getStatus() != null) {
				UtilXml.addValueElement(handler, "status", gameState.getStatus().getName());
			}
			if (StringTool.isProvided(game.getTeamHome())) {
				UtilXml.addValueElement(handler, "teamHome", game.getTeamHome().getName());
			}
			if (StringTool.isProvided(game.getTeamAway())) {
				UtilXml.addValueElement(handler, "teamAway", game.getTeamAway().getName());
			}
			if (game.getStarted() != null) {
				UtilXml.addValueElement(handler, "started", DateTool.formatTimestamp(game.getStarted()));
			}
			if (game.getFinished() != null) {
				UtilXml.addValueElement(handler, "finished", DateTool.formatTimestamp(game.getFinished()));
			}
			Session[] allSessions = getServer().getSessionManager().getSessionsForGameId(game.getId());
			Session[] spectatorSessions = getServer().getSessionManager().getSessionsOfSpectators(game.getId());
			AttributesImpl connectionAttributes = new AttributesImpl();
			UtilXml.addAttribute(connectionAttributes, "players", allSessions.length - spectatorSessions.length);
			UtilXml.addAttribute(connectionAttributes, "spectators", spectatorSessions.length);
			UtilXml.addEmptyElement(handler, "connections", connectionAttributes);
			UtilXml.endElement(handler, "game");
		}
		UtilXml.endElement(handler, "cache");
		return isOk;
	}

	private boolean handleStats(TransformerHandler handler) {
		boolean isOk = true;

		Session[] sessions = getServer().getSessionManager().getAllSessions();
		int openSessions = 0;
		int closedSessions = 0;
		for (Session session : sessions) {
			if (session.isOpen()) {
				openSessions++;
			} else {
				closedSessions++;
			}
		}

		GameState[] gameStates = getServer().getGameCache().allGameStates();
		int activeGames = 0;
		int inactiveGames = 0;
		for (GameState gameState : gameStates) {
			if (gameState.getStatus() == GameStatus.ACTIVE) {
				activeGames++;
			} else {
				inactiveGames++;
			}
		}

		Fortuna fortuna = getServer().getFortuna();

		ServerCommunication comms = getServer().getCommunication();

		UtilXml.startElement(handler, "stats");

		AttributesImpl commAttributes = new AttributesImpl();
		UtilXml.addAttribute(commAttributes, "queueLength", comms.getQueueLength());
		UtilXml.startElement(handler, "communication", commAttributes);
		UtilXml.endElement(handler, "communication");

		AttributesImpl fortunaAttributes = new AttributesImpl();
		UtilXml.addAttribute(fortunaAttributes, "rekeyings", fortuna.getRekeyings());
		UtilXml.addAttribute(fortunaAttributes, "numBytes", fortuna.getNumberOfBytes());
		UtilXml.startElement(handler, "fortuna", fortunaAttributes);
		UtilXml.endElement(handler, "fortuna");

		AttributesImpl cacheAttributes = new AttributesImpl();
		UtilXml.addAttribute(cacheAttributes, "size", gameStates.length);
		UtilXml.addAttribute(cacheAttributes, "active", activeGames);
		UtilXml.addAttribute(cacheAttributes, "inactive", inactiveGames);
		UtilXml.startElement(handler, "cache", cacheAttributes);
		UtilXml.endElement(handler, "cache");

		AttributesImpl sessionAttributes = new AttributesImpl();
		UtilXml.addAttribute(sessionAttributes, "size", sessions.length);
		UtilXml.addAttribute(sessionAttributes, "open", openSessions);
		UtilXml.addAttribute(sessionAttributes, "closed", closedSessions);
		UtilXml.startElement(handler, "sessions", sessionAttributes);
		UtilXml.endElement(handler, "sessions");

		UtilXml.endElement(handler, "stats");

		return isOk;
	}

	private boolean handleShutdown(TransformerHandler pHandler) {
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_INITIATED, _TIMESTAMP_FORMAT.format(new Date()));
		UtilXml.addEmptyElement(pHandler, _XML_TAG_SHUTDOWN, attributes);
		Thread stopThread = new Thread(() -> getServer().stop(0));
		stopThread.start();
		return true;
	}

	private boolean handleRefresh(TransformerHandler pHandler) {
		AttributesImpl attributes = new AttributesImpl();
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_INITIATED, _TIMESTAMP_FORMAT.format(new Date()));
		UtilXml.addEmptyElement(pHandler, _XML_TAG_REFRESH, attributes);
		return true;
	}


	private boolean handlePurge(TransformerHandler pHandler, Map<String, String[]> pParameters) {
		boolean isOk = true;
		AttributesImpl attributes = new AttributesImpl();
		String limitParameter = ArrayTool.firstElement(pParameters.get(_PARAMETER_LIMIT));
		if (!StringTool.isProvided(limitParameter)) {
			return false;
		}
		long limit;
		try {
			limit = new Long(limitParameter);
			if (limit < 1) {
				return false;
			}
		} catch (Exception e) {

			return false;
		}


		AdminList adminList = new AdminList();
		DbTestGameListQuery listQuery = (DbTestGameListQuery) getServer().getDbQueryFactory()
			.getStatement(DbStatementId.TEST_GAME_LIST_QUERY);
		listQuery.execute(adminList, limit);


		addGames(pHandler, attributes, adminList);

		String performParameter = ArrayTool.firstElement(pParameters.get(_PARAMETER_PERFORM));

		boolean perform = "true".equalsIgnoreCase(performParameter);

		if (perform) {
			for (AdminListEntry entry : adminList.getEntries()) {
				getServer().getCommunication().handleCommand(new InternalServerCommandDeleteGame(entry.getGameId(), true));
			}
		}

		UtilXml.addValueElement(pHandler, _XML_TAG_DELETED, perform);


		return isOk;
	}

	private void addGames(TransformerHandler pHandler, AttributesImpl attributes, AdminList adminList) {
		UtilXml.addAttribute(attributes, _XML_ATTRIBUTE_SIZE, adminList.size());
		if (adminList.size() > 0) {
			UtilXml.startElement(pHandler, _XML_TAG_LIST, attributes);
			for (AdminListEntry listEntry : adminList.getEntries()) {
				listEntry.addToXml(pHandler);
			}
			UtilXml.endElement(pHandler, _XML_TAG_LIST);
		} else {
			UtilXml.addEmptyElement(pHandler, _XML_TAG_LIST, attributes);
		}
	}

	private boolean checkResponse(String pResonse) {
		boolean isOk = (fLastChallenge != null);
		if (isOk) {
			byte[] md5Password = PasswordChallenge.fromHexString(fServer.getProperty(IServerProperty.ADMIN_PASSWORD));
			try {
				String response = PasswordChallenge.createResponse(fLastChallenge, md5Password);
				isOk = response.equals(pResonse);
			} catch (NoSuchAlgorithmException | IOException pE) {
				isOk = false;
			}
		}
		fLastChallenge = null;
		return isOk;
	}

}
