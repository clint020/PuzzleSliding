package com.planegg.puzzle;
public class Asukoht {
    private int X,Y, id;
    public Asukoht(int _id,int _x,int _y)
    {
       id=_id;
       setXY(_x,_y);
    }
    public int getX()
    {
        return X;
    }
    public int getY()
    {
        return Y;
    }
    public int getId()
    {
        return id;
    }
    public void setXY(int _x,int _y)
    {
        X=_x;
        Y=_y;

    }
}
