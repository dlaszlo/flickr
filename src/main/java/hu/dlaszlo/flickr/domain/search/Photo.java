package hu.dlaszlo.flickr.domain.search;

import java.io.Serializable;

/**
 * Created by dlasz on 2016. 02. 27..
 */
public class Photo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String id;
    private String owner;
    private String secret;
    private String server;
    private String farm;
    private String title;
    private int ispublic;
    private int isfriend;
    private int isfamily;
    private String url_o;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getOwner()
    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public String getSecret()
    {
        return secret;
    }

    public void setSecret(String secret)
    {
        this.secret = secret;
    }

    public String getServer()
    {
        return server;
    }

    public void setServer(String server)
    {
        this.server = server;
    }

    public String getFarm()
    {
        return farm;
    }

    public void setFarm(String farm)
    {
        this.farm = farm;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public int getIspublic()
    {
        return ispublic;
    }

    public void setIspublic(int ispublic)
    {
        this.ispublic = ispublic;
    }

    public int getIsfriend()
    {
        return isfriend;
    }

    public void setIsfriend(int isfriend)
    {
        this.isfriend = isfriend;
    }

    public int getIsfamily()
    {
        return isfamily;
    }

    public void setIsfamily(int isfamily)
    {
        this.isfamily = isfamily;
    }

    public String getUrl_o()
    {
        return url_o;
    }

    public void setUrl_o(String url_o)
    {
        this.url_o = url_o;
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

        Photo photo = (Photo) o;

        if (ispublic != photo.ispublic) {
            return false;
        }
        if (isfriend != photo.isfriend) {
            return false;
        }
        if (isfamily != photo.isfamily) {
            return false;
        }
        if (id != null ? !id.equals(photo.id) : photo.id != null) {
            return false;
        }
        if (owner != null ? !owner.equals(photo.owner) : photo.owner != null) {
            return false;
        }
        if (secret != null ? !secret.equals(photo.secret) : photo.secret != null) {
            return false;
        }
        if (server != null ? !server.equals(photo.server) : photo.server != null) {
            return false;
        }
        if (farm != null ? !farm.equals(photo.farm) : photo.farm != null) {
            return false;
        }
        if (title != null ? !title.equals(photo.title) : photo.title != null) {
            return false;
        }
        return url_o != null ? url_o.equals(photo.url_o) : photo.url_o == null;

    }

    @Override
    public int hashCode()
    {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (owner != null ? owner.hashCode() : 0);
        result = 31 * result + (secret != null ? secret.hashCode() : 0);
        result = 31 * result + (server != null ? server.hashCode() : 0);
        result = 31 * result + (farm != null ? farm.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + ispublic;
        result = 31 * result + isfriend;
        result = 31 * result + isfamily;
        result = 31 * result + (url_o != null ? url_o.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Photo{" +
                "id='" + id + '\'' +
                ", owner='" + owner + '\'' +
                ", secret='" + secret + '\'' +
                ", server='" + server + '\'' +
                ", farm='" + farm + '\'' +
                ", title='" + title + '\'' +
                ", ispublic=" + ispublic +
                ", isfriend=" + isfriend +
                ", isfamily=" + isfamily +
                ", url_o='" + url_o + '\'' +
                '}';
    }
}
