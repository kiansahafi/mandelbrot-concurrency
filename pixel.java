public class pixel {
    private final int x;
    private final int y;
    private String colour;
    private String visualisationColour;

    public pixel(int x, int y){
        this.x = x;
        this.y = y;
    }
    public int getX(){
        return x;
    }
    public int getY(){
        return y;
    }

    public String getColour(){
        return colour;
    }

    public String getVisualisationColour(){
        return visualisationColour;
    }

    public void setColour(String colour){
        this.colour = colour;
    }

    public void setVisualisationColour(String colour){
        this.visualisationColour = colour;
    }
}


