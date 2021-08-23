package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.aliyun.log.producer.Producer;
import com.aliyun.openservices.log.Client;
import com.aliyun.openservices.log.common.Histogram;
import com.aliyun.openservices.log.common.LogItem;
import com.aliyun.openservices.log.common.QueriedLog;
import com.aliyun.openservices.log.request.GetHistogramsRequest;
import com.aliyun.openservices.log.request.GetLogsRequest;
import com.aliyun.openservices.log.response.GetHistogramsResponse;
import com.aliyun.openservices.log.response.GetLogsResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository("aliyunlogMetricsRepository")
public class AliyunlogMetricsRepository implements MetricsRepository<MetricEntity> {

    private final Logger logger = LoggerFactory.getLogger(AliyunlogMetricsRepository.class);


    @Value("${aliyunlog.project}")
    private String project;

    @Value("${aliyunlog.store}")
    private String store;

    @Value("${aliyunlog.topic}")
    private String topic;

    @Value("${aliyunlog.source}")
    private String source;

    @Autowired
    Producer producer;

    @Autowired
    Client client;

    @Override
    public void save(MetricEntity metric) {
        LogItem logItem = new LogItem();
        try {
            logItem.PushBack("app", metric.getApp());
            logItem.PushBack("id", String.valueOf(metric.getId()));
            logItem.PushBack("gmt_create", formatDate(metric.getGmtCreate()));
            logItem.PushBack("gmt_modified", formatDate(metric.getGmtModified()));
            logItem.PushBack("timestamp", formatDate(metric.getTimestamp()));
            logItem.PushBack("resource", metric.getResource());
            logItem.PushBack("pass_qps", String.valueOf(metric.getPassQps()));
            logItem.PushBack("success_qps", String.valueOf(metric.getSuccessQps()));
            logItem.PushBack("block_qps", String.valueOf(metric.getBlockQps()));
            logItem.PushBack("exception_qps", String.valueOf(metric.getExceptionQps()));
            logItem.PushBack("rt", String.valueOf(metric.getRt()));
            logItem.PushBack("count", String.valueOf(metric.getCount()));
            producer.send(project, store, topic, source, logItem);
        } catch (Exception e) {
            logger.error("Failed to send log [project={}, store={}, topic={}, source={}, logItem={}]", project, store, topic, source, logItem, e);
        }
    }

    @Override
    public void saveAll(Iterable<MetricEntity> metrics) {
        if (metrics == null) {
            return;
        }
        try {
            metrics.forEach(this::save);
        } finally {
        }
    }

    @Override
    public List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {
        List<MetricEntity> results = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("*");
        if (StringUtils.isNotEmpty(app)) {
            query.append(" and ").append("app:\"").append(app).append("\"");
        }
        if (StringUtils.isNotEmpty(resource)) {
            query.append(" and ").append("resource:\"").append(resource).append("\"");
        }
        int start = Math.toIntExact(startTime / 1000);
        int stop = Math.toIntExact(endTime / 1000);
        String requestContent = query.toString();
        try {
            GetHistogramsRequest histogramsRequest = new GetHistogramsRequest(project, store, topic, requestContent, start, stop);
            GetHistogramsResponse histogramsResponse = client.GetHistograms(histogramsRequest);
            ArrayList<Histogram> histogramArrayList = histogramsResponse.GetHistograms();
            if (histogramArrayList != null && histogramArrayList.size() > 0) {
                GetLogsRequest logsRequest = new GetLogsRequest(project, store, start, stop, topic, requestContent, 0, 100000, true);
                GetLogsResponse logsResponse = client.GetLogs(logsRequest);
                ArrayList<QueriedLog> logs = logsResponse.GetLogs();
                if (logs != null && logs.size() > 0) {
                    for (QueriedLog log : logs) {
                        LogItem logItem = log.GetLogItem();
                        String json = logItem.ToJsonString();
                        JSONObject jso = JSONObject.parseObject(json);
                        MetricEntity metric = new MetricEntity();
                        metric.setApp(jso.getString("app"));
                        metric.setId(jso.getLong("id"));
                        metric.setGmtCreate(DateUtils.parseDate(jso.getString("gmt_create"), new String[]{"yyyy-MM-dd HH:mm:ss"}));
                        metric.setGmtModified(DateUtils.parseDate(jso.getString("gmt_modified"), new String[]{"yyyy-MM-dd HH:mm:ss"}));
                        metric.setTimestamp(DateUtils.parseDate(jso.getString("timestamp"), new String[]{"yyyy-MM-dd HH:mm:ss"}));
                        metric.setResource(jso.getString("resource"));
                        metric.setPassQps(Long.valueOf(jso.getString("pass_qps")));
                        metric.setSuccessQps(Long.valueOf(jso.getString("success_qps")));
                        metric.setBlockQps(Long.valueOf(jso.getString("block_qps")));
                        metric.setExceptionQps(Long.valueOf(jso.getString("exception_qps")));
                        metric.setRt(Double.valueOf(jso.getString("rt")));
                        metric.setCount(Integer.valueOf(jso.getString("count")));
                        results.add(metric);
                    }
                }
            }
        } catch (Exception e) {

        }
        return results;
    }

    @Override
    public List<String> listResourcesOfApp(String app) {
        List<String> results = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("*");
        if (StringUtils.isNotEmpty(app)) {
            query.append(" and ").append("app:\"").append(app).append("\"");
        }
        LocalDateTime ldt1 = LocalDateTime.now();
        LocalDateTime ldt2 = ldt1.minusDays(7);

        Date date2 = Date.from(ldt2.atZone(ZoneId.systemDefault()).toInstant());
        Date date1 = Date.from(ldt1.atZone(ZoneId.systemDefault()).toInstant());

        int start = Math.toIntExact(date2.getTime() / 1000);
        int stop = Math.toIntExact(date1.getTime() / 1000);
        String requestContent = query.toString();
        try {
            GetHistogramsRequest histogramsRequest = new GetHistogramsRequest(project, store, topic, requestContent, start, stop);
            GetHistogramsResponse histogramsResponse = client.GetHistograms(histogramsRequest);
            ArrayList<Histogram> histogramArrayList = histogramsResponse.GetHistograms();
            if (histogramArrayList != null && histogramArrayList.size() > 0) {
                GetLogsRequest logsRequest = new GetLogsRequest(project, store, start, stop, topic, requestContent, 0, 100000, true);
                GetLogsResponse logsResponse = client.GetLogs(logsRequest);
                ArrayList<QueriedLog> logs = logsResponse.GetLogs();
                if (logs != null && logs.size() > 0) {
                    for (QueriedLog log : logs) {
                        LogItem logItem = log.GetLogItem();
                        String json = logItem.ToJsonString();
                        JSONObject jso = JSONObject.parseObject(json);
                        results.add(jso.getString("resource"));
                    }
                }
            }
        } catch (Exception e) {

        }
        return results;
    }

    private String formatDate(Date dt) {
        if (dt == null) {
            return "";
        }
        Instant instant = dt.toInstant();
        LocalDateTime ldt = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ldt.format(fmt);
    }
}
