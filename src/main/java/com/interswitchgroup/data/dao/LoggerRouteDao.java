package com.interswitchgroup.data.dao;

import com.interswitchgroup.data.dto.Log;
import com.interswitchgroup.proxy.MockContext;
import com.interswitchgroup.util.consts.AppConstants;
import com.interswitchgroup.util.enums.LoggerType;
import com.interswitchgroup.util.io.StringUtility;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;

import java.util.ArrayList;
import java.util.List;

import static com.interswitchgroup.util.consts.AppConstants.LogType;

public class LoggerRouteDao {
    public static String LogSchema = "Log";

    public static List<Log> fetch(long startDate, long endDate) {
        StoreTransaction txn = MockContext.store.beginTransaction();
        List<Log> logs = new ArrayList<>();
        try {
            EntityIterable logDate = txn.find(LogSchema, "LogDate", startDate, endDate);
            for (Entity entity : logDate) {
                Log log = getLog(entity);
                logs.add(log);
            }
        } catch (Exception exc) {
            return null;
        } finally {
            if (txn != null & !txn.isFinished()) {
                txn.abort();
            }
        }
        return logs;
    }

    private static Log getLog(Entity entity) {
        Log log = new Log();
        log.setLogType(StringUtility.parseString(entity.getProperty(LogType)));
        log.setLogMessage(StringUtility.parseString(entity.getProperty(AppConstants.LogMessage)));
        log.setLogStackTrace(StringUtility.parseString(entity.getProperty(AppConstants.LogStackTrace)));
        String dateStr = StringUtility.parseString(entity.getProperty(AppConstants.LogDate));
        log.setLogDate(Long.valueOf(dateStr));
        return log;
    }

    public static void log(Log log) {
        if (log != null) {
            StoreTransaction txn = MockContext.store.beginTransaction();
            try {
                Entity route = txn.newEntity(LogSchema);
                route.setProperty(AppConstants.LogMessage, log.getLogMessage());
                route.setProperty(AppConstants.LogStackTrace, StringUtility.isEmpty(log.getLogStackTrace(), ""));
                route.setProperty(LogType, StringUtility.isEmpty(log.getLogType(), LoggerType.DEFAULT.logType));
                route.setProperty(AppConstants.LogDate, log.getLogDate());
                txn.commit();
            } catch (Exception exc) {
                exc.printStackTrace();
            } finally {
                if (txn != null & !txn.isFinished()) {
                    txn.abort();
                }
            }
        }
    }
}
