package hu.dlaszlo.flickr.service;

import hu.dlaszlo.flickr.domain.backup.BackupEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dlasz on 2016. 02. 28..
 */
public class BackupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupService.class);

    private String database;

    private static BackupService backupService;

    private BackupService() {
        //
    }

    public static BackupService getInstance() {
        synchronized (BackupService.class) {
            if (backupService == null) {
                backupService = new BackupService();
            }
        }
        return backupService;
    }

    private synchronized Connection getConnection() {
        Connection connection;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + database);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public synchronized void init(String database) {
        this.database = database;
        try (Connection c = getConnection()) {
            try (Statement stmt = c.createStatement()) {
                String sql = "create table backup_entries (" +
                        "  photo_id     text      primary key,     " +
                        "  path         text      not null unique, " +
                        "  tag          text      not null unique, " +
                        "  mime_type    text      not null,        " +
                        "  file_time    timestamp not null,        " +
                        "  md5          text      not null,        " +
                        "  download_url text,                      " +
                        "  backup_time  timestamp not null )       ";
                stmt.executeUpdate(sql);
            }
        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
        }
    }

    public synchronized List<BackupEntry> list() {
        List<BackupEntry> backupEntries = new ArrayList<>();
        try (Connection c = getConnection()) {
            try (Statement stmt = c.createStatement()) {
                ResultSet rs = stmt.executeQuery("select * from backup_entries order by path");
                while (rs.next()) {
                    String photoId = rs.getString("photo_id");
                    String path = rs.getString("path");
                    String tag = rs.getString("tag");
                    String mimeType = rs.getString("mime_type");
                    java.sql.Timestamp fileTime = rs.getTimestamp("file_time");
                    String md5 = rs.getString("md5");
                    String downloadUrl = rs.getString("download_url");
                    java.sql.Timestamp backupTime = rs.getTimestamp("backup_time");
                    BackupEntry backupEntry = new BackupEntry(
                            photoId,
                            path,
                            tag,
                            mimeType,
                            new java.util.Date(fileTime.getTime()),
                            md5,
                            downloadUrl,
                            new java.util.Date(backupTime.getTime()));
                    backupEntries.add(backupEntry);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return backupEntries;
    }

    public synchronized void save(BackupEntry backupEntry) {
        try (Connection c = getConnection()) {
            String sql = "insert into backup_entries " +
                    " (photo_id, path, tag, mime_type, file_time, md5, download_url, backup_time) " +
                    " values(?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, backupEntry.getPhotoId());
                ps.setString(2, backupEntry.getPath());
                ps.setString(3, backupEntry.getTag());
                ps.setString(4, backupEntry.getMimeType());
                ps.setTimestamp(5, new java.sql.Timestamp(backupEntry.getFileTime().getTime()));
                ps.setString(6, backupEntry.getMd5());
                ps.setString(7, backupEntry.getDownloadUrl());
                ps.setTimestamp(8, new java.sql.Timestamp(backupEntry.getBackupTime().getTime()));
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized int update(BackupEntry backupEntry) {
        int cnt;
        try (Connection c = getConnection()) {
            String sql = "update backup_entries " +
                    "set photo_id     = ?, " +
                    "    tag          = ?, " +
                    "    mime_type    = ?, " +
                    "    file_time    = ?, " +
                    "    md5          = ?, " +
                    "    download_url = ?, " +
                    "    backup_time  = ?  " +
                    "where path       = ?";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, backupEntry.getPhotoId());
                ps.setString(2, backupEntry.getTag());
                ps.setString(3, backupEntry.getMimeType());
                ps.setTimestamp(4, new java.sql.Timestamp(backupEntry.getFileTime().getTime()));
                ps.setString(5, backupEntry.getMd5());
                ps.setString(6, backupEntry.getDownloadUrl());
                ps.setTimestamp(7, new java.sql.Timestamp(backupEntry.getBackupTime().getTime()));
                ps.setString(8, backupEntry.getPath());
                cnt = ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return cnt;
    }

}
