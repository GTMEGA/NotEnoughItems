package codechicken.nei;

public abstract class ButtonCycled extends Button
{    
    public ButtonCycled() {}

    public ButtonCycled(int num) {
        icons = new Image[num];
    }
    @Override
    public Image getRenderIcon()
    {
        return icons[index];
    }

    public int index;
    public Image[] icons;
}
