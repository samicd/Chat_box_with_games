package chatGUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

class Square extends JPanel {
    JLabel label = new JLabel();
    public int[] position;

    public Square(int[] position) {

        if ((position[0] + position[1]) % 2 == 0) {
            if (position[0] == 0 && position[1] == 0){

                setBackground(java.awt.Color.RED.darker());
                setLayout(new GridBagLayout());
                label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 40));
                add(label);
            } else {
                setBackground(java.awt.Color.ORANGE.brighter());
                setLayout(new GridBagLayout());
                label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 40));
                add(label);
            }
        }
        else {
            if (position[0] == 0 && position[1] == 9){
                setBackground(java.awt.Color.GREEN.darker());
                setLayout(new GridBagLayout());
                label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 40));
                add(label);
            } else {
                setBackground(java.awt.Color.CYAN);
                setLayout(new GridBagLayout());
                label.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 40));
                add(label);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {


        super.paintComponent(g);
        if (position[0]==2&&position[1]==0)
            g.drawImage(makeImage1(), 0, 0, null);
        if(position[0]==2&&position[1]==1)
            g.drawImage(makeImage2(), 0, 0, null);
        if(position[0]==2&&position[1]==2)
            g.drawImage(makeImage3(), 0, 0, null);
        if(position[0]==2&&position[1]==3)
            g.drawImage(makeImage4(), 0, 0, null);
        if(position[0]==5&&position[1]==2)
            g.drawImage(makeImage5(), 0, 0, null);
        if(position[0]==5&&position[1]==3)
            g.drawImage(makeImage6(), 0, 0, null);
        if(position[0]==4&&position[1]==3)
            g.drawImage(makeImage7(), 0, 0, null);
        if(position[0]==4&&position[1]==4)
            g.drawImage(makeImage8(), 0, 0, null);
        if(position[0]==7&&position[1]==7)
            g.drawImage(makeImage9(), 0, 0, null);
        if(position[0]==7&&position[1]==8)
            g.drawImage(makeImage10(), 0, 0, null);
        if(position[0]==0&&position[1]==5)
            g.drawImage(makeImage11(), 0, 0, null);
        if(position[0]==0&&position[1]==6)
            g.drawImage(makeImage12(), 0, 0, null);
        if(position[0]==0&&position[1]==7)
            g.drawImage(makeImage13(), 0, 0, null);
        if(position[0]==8&&position[1]==4)
            g.drawImage(makeImage14(), 0, 0, null);
        if(position[0]==9&&position[1]==4)
            g.drawImage(makeImage15(), 0, 0, null);
        if(position[0]==8&&position[1]==5)
            g.drawImage(makeImage16(), 0, 0, null);
        if(position[0]==9&&position[1]==5)
            g.drawImage(makeImage17(), 0, 0, null);
    }

    public java.awt.Image makeImage1() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake1Top.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage2() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake1Top2.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage3() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake1Mid2.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }

    public java.awt.Image makeImage4() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake1Bottom.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }

    public java.awt.Image makeImage5() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake2Top.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage6() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake2Mid1.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage7() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake2Mid2.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage8() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake2Bottom.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage9() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake3Top.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage10() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Snake3Bottom.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage11() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Ladder1Top.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage12() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Ladder1Mid.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage13() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Ladder1Bottom.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage14() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Ladder2Top.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage15() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Ladder2TopRight.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage16() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Ladder2TopLeft.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }
    public java.awt.Image makeImage17() {
        java.awt.Image Image;
        try
        {Image = ImageIO.read(new File("src/images/Ladder2Bottom.png"));}
        catch (IOException e)
        {
            Image = null;
        }
        return Image;
    }

    public void setText(char text) {
        label.setForeground(text == 'X' ? java.awt.Color.BLUE : java.awt.Color.RED);
        label.setText(text + "");
    }
}
