package hu.dlaszlo.flickr.domain.backup;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by dlasz on 2016. 02. 28..
 */
public class BackupEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private String photoId;
    private String path;
    private String tag;
    private String mimeType;
    private Date fileTime;
    private String md5;
    private String downloadUrl;
    private Date backupTime;

    public BackupEntry() {
        //
    }

    public BackupEntry(String photoId, String path, String tag, String mimeType, Date fileTime, String md5, String downloadUrl, Date backupTime) {
        this.photoId = photoId;
        this.path = path;
        this.tag = tag;
        this.mimeType = mimeType;
        this.fileTime = fileTime;
        this.md5 = md5;
        this.downloadUrl = downloadUrl;
        this.backupTime = backupTime;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public Date getFileTime() {
        return fileTime;
    }

    public void setFileTime(Date fileTime) {
        this.fileTime = fileTime;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Date getBackupTime() {
        return backupTime;
    }

    public void setBackupTime(Date backupTime) {
        this.backupTime = backupTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BackupEntry that = (BackupEntry) o;

        if (photoId != null ? !photoId.equals(that.photoId) : that.photoId != null) return false;
        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
        if (mimeType != null ? !mimeType.equals(that.mimeType) : that.mimeType != null) return false;
        if (fileTime != null ? !fileTime.equals(that.fileTime) : that.fileTime != null) return false;
        if (md5 != null ? !md5.equals(that.md5) : that.md5 != null) return false;
        if (downloadUrl != null ? !downloadUrl.equals(that.downloadUrl) : that.downloadUrl != null) return false;
        return backupTime != null ? backupTime.equals(that.backupTime) : that.backupTime == null;

    }

    @Override
    public int hashCode() {
        int result = photoId != null ? photoId.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        result = 31 * result + (fileTime != null ? fileTime.hashCode() : 0);
        result = 31 * result + (md5 != null ? md5.hashCode() : 0);
        result = 31 * result + (downloadUrl != null ? downloadUrl.hashCode() : 0);
        result = 31 * result + (backupTime != null ? backupTime.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BackupEntry{" +
                "photoId='" + photoId + '\'' +
                ", path='" + path + '\'' +
                ", tag='" + tag + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", fileDate=" + fileTime +
                ", md5='" + md5 + '\'' +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", backupTime=" + backupTime +
                '}';
    }
}

