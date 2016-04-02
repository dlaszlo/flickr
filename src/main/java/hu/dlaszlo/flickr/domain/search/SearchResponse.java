package hu.dlaszlo.flickr.domain.search;

import java.io.Serializable;

/**
 * Created by dlasz on 2016. 02. 27..
 */
public class SearchResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Photos photos;
    private String stat;

    public Photos getPhotos()
    {
        return photos;
    }

    public void setPhotos(Photos photos)
    {
        this.photos = photos;
    }

    public String getStat()
    {
        return stat;
    }

    public void setStat(String stat)
    {
        this.stat = stat;
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

        SearchResponse searchResponse = (SearchResponse) o;

        if (photos != null ? !photos.equals(searchResponse.photos) : searchResponse.photos != null) {
            return false;
        }
        return stat != null ? stat.equals(searchResponse.stat) : searchResponse.stat == null;
    }

    @Override
    public int hashCode()
    {
        int result = photos != null ? photos.hashCode() : 0;
        result = 31 * result + (stat != null ? stat.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "SearchResponse{" +
                "photos=" + photos +
                ", stat='" + stat + '\'' +
                '}';
    }
}
