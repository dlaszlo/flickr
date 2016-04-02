package hu.dlaszlo.flickr.domain.size;

import java.io.Serializable;

/**
 * Created by dlasz on 2016. 03. 03..
 */
public class Size implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String label;
    private int width;
    private int height;
    private String source;
    private String url;
    private String media;

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }

    public int getHeight()
    {
        return height;
    }

    public void setHeight(int height)
    {
        this.height = height;
    }

    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }

    public String getUrl()
    {
        return url;
    }

    public void setUrl(String url)
    {
        this.url = url;
    }

    public String getMedia()
    {
        return media;
    }

    public void setMedia(String media)
    {
        this.media = media;
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

        Size size = (Size) o;

        if (width != size.width) {
            return false;
        }
        if (height != size.height) {
            return false;
        }
        if (label != null ? !label.equals(size.label) : size.label != null) {
            return false;
        }
        if (source != null ? !source.equals(size.source) : size.source != null) {
            return false;
        }
        if (url != null ? !url.equals(size.url) : size.url != null) {
            return false;
        }
        return media != null ? media.equals(size.media) : size.media == null;

    }

    @Override
    public int hashCode()
    {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (media != null ? media.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "Size{" +
                "label='" + label + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", source='" + source + '\'' +
                ", url='" + url + '\'' +
                ", media='" + media + '\'' +
                '}';
    }
}
