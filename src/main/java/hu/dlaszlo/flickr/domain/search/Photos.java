package hu.dlaszlo.flickr.domain.search;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dlasz on 2016. 02. 27..
 */
public class Photos implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int page;
    private int pages;
    private int perpage;
    private int total;
    private List<Photo> photo;

    public int getPage()
    {
        return page;
    }

    public void setPage(int page)
    {
        this.page = page;
    }

    public int getPages()
    {
        return pages;
    }

    public void setPages(int pages)
    {
        this.pages = pages;
    }

    public int getPerpage()
    {
        return perpage;
    }

    public void setPerpage(int perpage)
    {
        this.perpage = perpage;
    }

    public int getTotal()
    {
        return total;
    }

    public void setTotal(int total)
    {
        this.total = total;
    }

    public List<Photo> getPhoto()
    {
        return photo;
    }

    public void setPhoto(List<Photo> photo)
    {
        this.photo = photo;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Photos photos = (Photos) o;

        if (page != photos.page) {
            return false;
        }
        if (pages != photos.pages) {
            return false;
        }
        if (perpage != photos.perpage) {
            return false;
        }
        if (total != photos.total) {
            return false;
        }
        return photo != null ? photo.equals(photos.photo) : photos.photo == null;

    }

    @Override
    public int hashCode()
    {
        int result = page;
        result = 31 * result + pages;
        result = 31 * result + perpage;
        result = 31 * result + total;
        result = 31 * result + (photo != null ? photo.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Photos{" +
                "page=" + page +
                ", pages=" + pages +
                ", perpage=" + perpage +
                ", total=" + total +
                ", photo=" + photo +
                '}';
    }
}
