package hu.dlaszlo.flickr.domain.size;

import java.io.Serializable;

/**
 * Created by dlasz on 2016. 03. 03..
 */
public class SizeResponse implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Sizes sizes;
    private String stat;

    public Sizes getSizes()
    {
        return sizes;
    }

    public void setSizes(Sizes sizes)
    {
        this.sizes = sizes;
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

        SizeResponse that = (SizeResponse) o;

        if (sizes != null ? !sizes.equals(that.sizes) : that.sizes != null) {
            return false;
        }
        return stat != null ? stat.equals(that.stat) : that.stat == null;

    }

    @Override
    public int hashCode()
    {
        int result = sizes != null ? sizes.hashCode() : 0;
        result = 31 * result + (stat != null ? stat.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "SizeResponse{" +
                "sizes=" + sizes +
                ", stat='" + stat + '\'' +
                '}';
    }
}
