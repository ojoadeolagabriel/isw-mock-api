package com.interswitchgroup.data.dao;

import com.interswitchgroup.data.dto.Log;
import com.interswitchgroup.proxy.MockContext;
import com.interswitchgroup.util.consts.AppConstants;
import com.interswitchgroup.util.enums.LoggerType;
import com.interswitchgroup.util.io.StringUtility;
import jetbrains.exodus.entitystore.Entity;
import jetbrains.exodus.entitystore.EntityIterable;
import jetbrains.exodus.entitystore.StoreTransaction;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.interswitchgroup.util.consts.AppConstants.LogType;

public class LoggerRouteDao {
    public static String LogSchema = "Log";

    public static Log fetchByGuid(String uuid){
        StoreTransaction txn = MockContext.store.beginTransaction();
        List<Log> logs = new ArrayList<>();
        try {
            EntityIterable logDate = txn
                    .find(LogSchema, "LogGuid", uuid).reverse();
            for (Entity entity : logDate) {
                Log log = getLog(entity);
                return log;
            }
        } catch (Exception exc) {
            return null;
        } finally {
            if (txn != null & !txn.isFinished()) {
                txn.abort();
            }
        }
        return null;
    }

    public static List<Log> fetch(long startDate, long endDate) {
        StoreTransaction txn = MockContext.store.beginTransaction();
        List<Log> logs = new ArrayList<>();
        try {
            EntityIterable logDate = txn
                    .find(LogSchema, "LogDate", startDate, endDate).reverse();
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

    public static List<Log> fetchAndFilterByDesc(String id, long startDate, long endDate) {
        StoreTransaction txn = MockContext.store.beginTransaction();
        List<Log> logs = new ArrayList<>();
        try {
            List<Entity> logsResult = new ArrayList<>();
            txn.find(LogSchema, "LogDate", startDate, endDate)
                .reverse()
                .forEach(c -> {
                    if(!StringUtils.isEmpty(id)) {
                        if (c.getProperty("LogMessage").toString().contains(id)) {
                            logsResult.add(c);
                        } else {

                        }
                    }else{
                        logsResult.add(c);
                    }
                });

            for (Entity entity : logsResult) {
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

        log.setLogUrlInformation(StringUtility.parseString(entity.getProperty(AppConstants.LogUrlInformation)));
        log.setLogRequestData(StringUtility.parseString(entity.getProperty(AppConstants.LogRequestData)));
        log.setLogResponseData(StringUtility.parseString(entity.getProperty(AppConstants.LogResponseData)));
        log.setGuid(StringUtility.parseString(entity.getProperty(AppConstants.LogGuid), ""));

        String dateStr = StringUtility.parseString(entity.getProperty(AppConstants.LogDate));
        log.setLogDate(Long.valueOf(dateStr));
        return log;
    }

    public static boolean log(Log log) {
        if (log != null) {
            StoreTransaction txn = MockContext.store.beginTransaction();
            try {
                Entity route = txn.newEntity(LogSchema);
                route.setProperty(AppConstants.LogMessage, log.getLogMessage());
                route.setProperty(AppConstants.LogStackTrace, StringUtility.isEmpty(log.getLogStackTrace(), ""));
                route.setProperty(AppConstants.LogRequestData, StringUtility.isEmpty(log.getLogRequestData(), ""));
                route.setProperty(AppConstants.LogResponseData, StringUtility.isEmpty(log.getLogResponseData(), ""));
                route.setProperty(AppConstants.LogUrlInformation, StringUtility.isEmpty(log.getLogUrlInformation(), ""));
                route.setProperty(LogType, StringUtility.isEmpty(log.getLogType(), LoggerType.DEFAULT.logType));
                route.setProperty(AppConstants.LogGuid, UUID.randomUUID().toString().toLowerCase());
                route.setProperty(AppConstants.LogDate, log.getLogDate());
                txn.commit();
                return true;
            } catch (Exception exc) {
                exc.printStackTrace();
                return false;
            } finally {
                if (txn != null & !txn.isFinished()) {
                    txn.abort();
                }
            }
        }
        return false;
    }
}
