package hu.dlaszlo.flickr.domain.size;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dlasz on 2016. 03. 03..
 */
public class Sizes  implements Serializable {
    private static final long serialVersionUID = 1L;

    private int canblog;
    private int canprint;
    private int candownload;
    private List<Size> size;

    public int getCanblog() {
        return canblog;
    }

    public void setCanblog(int canblog) {
        this.canblog = canblog;
    }

    public int getCanprint() {
        return canprint;
    }

    public void setCanprint(int canprint) {
        this.canprint = canprint;
    }

    public int getCandownload() {
        return candownload;
    }

    public void setCandownload(int candownload) {
        this.candownload = candownload;
    }

    public List<Size> getSize() {
        return size;
    }

    public void setSize(List<Size> size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sizes sizes = (Sizes) o;

        if (canblog != sizes.canblog) return false;
        if (canprint != sizes.canprint) return false;
        if (candownload != sizes.candownload) return false;
        return size != null ? size.equals(sizes.size) : sizes.size == null;

    }

    @Override
    public int hashCode() {
        int result = canblog;
        result = 31 * result + canprint;
        result = 31 * result + candownload;
        result = 31 * result + (size != null ? size.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Sizes{" +
                "canblog=" + canblog +
                ", canprint=" + canprint +
                ", candownload=" + candownload +
                ", size=" + size +
                '}';
    }
}
