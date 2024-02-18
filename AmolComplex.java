import java.awt.*;

public class AmolComplex extends Canvas {

   int counter = 0;
   int Steps;
   int gameEntries = 0;
   boolean isStopped = false;
   boolean isStarted = false;
   boolean isWinner = false;
   int nAtoms = 6;


   public AmolComplex (int i, int Na) {
      Steps = i;
      nAtoms = Na;
   }

   public void changeModel(int Na) {
      nAtoms = Na;
   }

   public void setCounter(int i) {
      counter = i;
   }

   public void setStarted() {
      isStarted = true;
      isStopped = false;
      isWinner = false;
      counter = 0;
   }

   public void setStopped(int entries) {
      isStopped = true;
      isStarted = false;
      gameEntries = entries;
   }

   public boolean getStarted() {
      return isStarted;
   }

   public void setWinner(int entries) {
      isWinner = true;
      gameEntries = entries;
      // DBG System.out.println("gameEntries = " + gameEntries);
   }

   private void constructHexagon(double[] x, double[] y, int R) {
      x[0] = (int)(R * Math.cos(3 * Math.PI / 6));
      y[0] = (int)(R * Math.sin(3 * Math.PI / 6));

      x[1] = (int)(R * Math.cos(5 * Math.PI / 6));
      y[1] = (int)(R * Math.sin(5 * Math.PI / 6));

      x[2] = (int)(R * Math.cos(7 * Math.PI / 6));
      y[2] = (int)(R * Math.sin(7 * Math.PI / 6));

      x[3] = (int)(R * Math.cos(9 * Math.PI / 6));
      y[3] = (int)(R * Math.sin(9 * Math.PI / 6));

      x[4] = (int)(R * Math.cos(11 * Math.PI / 6));
      y[4] = (int)(R * Math.sin(11 * Math.PI / 6));

      x[5] = (int)(R * Math.cos(13 * Math.PI / 6));
      y[5] = (int)(R * Math.sin(13 * Math.PI / 6));
   }

   private void constructNaphtalene(double[] x, double[] y, int R) {
      int shift = (nAtoms > 6) ? (int)(R * Math.cos(Math.PI / 6)) : 0;

      for (int i = 0; i < nAtoms; i++) {
         int k = i + 1;
         x[i] = (int)(R * Math.cos((2 * k + 1) * Math.PI / 6)) + shift;
         y[i] = (int)(R * Math.sin((2 * k + 1) * Math.PI / 6));

         if (i > 5) {
            x[i] -= (int)(2 * R * Math.cos(Math.PI / 6));
         }
      }
   }

   Image offScreenBuffer;

   public void update(Graphics g) {
      Graphics gr; 
      if (offScreenBuffer==null ||
          (! (offScreenBuffer.getWidth(this) == this.getSize().width
          && offScreenBuffer.getHeight(this) == this.getSize().height))) {
         offScreenBuffer = this.createImage(getSize().width, getSize().height);
      }

      gr = offScreenBuffer.getGraphics();

      Color oldColor = gr.getColor();
      gr.setColor(Color.black);
      gr.fillRect(0, 0, getSize().width, getSize().height);
      gr.setColor(oldColor);

      paint(gr); 
      g.drawImage(offScreenBuffer, 0, 0, this);     
   }

   public void paint(Graphics g) {
      int width = getSize().width;
      int height = getSize().height;
      int d = Math.min(width, height) / Steps;

      g.setColor(Color.green);

      if (isWinner) {
         Font oldFont = g.getFont();
         g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));

         String text[] = {"Congratulations! You won!",
                          "Your Wave Function yields",
                          "the correct Ground State",
                          "Energy after " + gameEntries + " steps."};

         FontMetrics fm = g.getFontMetrics();
         int h = height / (text.length + 1);

         for (int i = 0; i < text.length; i++) {
            int x, y;
            x = (width - fm.stringWidth(text[i])) / 2;
            y = (i + 1) * h + fm.getHeight() / 2 - 1;
            g.drawString (text[i], x, y);
         }

         g.setFont(oldFont);

         return;
      }
      else if (isStopped) {
         Font oldFont = g.getFont();
         g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));

         String text[] = {"Sorry! Your Wave Function",
                          "does not yield the correct",
                          "Ground State Energy",
                          "after " + gameEntries + " steps."};

         FontMetrics fm = g.getFontMetrics();
         int h = height / (text.length + 1);

         int x, y;
         for (int i = 0; i < text.length; i++) {
            x = (width - fm.stringWidth(text[i])) / 2;
            y = (i + 1) * h + fm.getHeight() / 2 - 1;
            g.drawString (text[i], x, y);
         }

         g.setFont(oldFont);

         return;
      }

      if (isStopped == false && isStarted == false) {
         String text[] = {"Press Start button to begin.",
                          "Then choose appropriate configuration",
                          "in the Top Left window and press Enter.",
                          "You can preview your results", 
                          "in the Bottom Left window."};

         FontMetrics fm = g.getFontMetrics();
         int h = height / (text.length + 1);

         int x, y;
         for (int i = 0; i < text.length; i++) {
            x = (width - fm.stringWidth(text[i])) / 2;
            y = (i + 1) * h + fm.getHeight() / 2 - 1;
            g.drawString (text[i], x, y);
         }

         return;
      }

      if (isStarted == false) return;

      double[] benzene_x = new double [nAtoms];
      double[] benzene_y = new double [nAtoms];

      int R = Math.min(width, height) / 3;

      if (nAtoms > 6)
         constructNaphtalene(benzene_x, benzene_y, R);
      else
         constructHexagon(benzene_x, benzene_y, R);

      int[] trans_x = new int [nAtoms];
      int[] trans_y = new int [nAtoms];

      Polygon p = new Polygon();
      Polygon p2 = new Polygon();

      double Ze = 6 * height / 6; // -160; // 5/6

      if (nAtoms > 6) Ze = 10 * height / 6;

      int Y = 3 * height / 10; // 50;

      if (nAtoms > 6) Y = height / 3;

      int path = height / 2 + Y;

      g.drawString("H", (width - d)/ 2, counter * path / Steps - 1);
      g.fillOval((width - d)/ 2, counter * path / Steps, d, d);

      g.translate(width / 2 - 1, height / 2 - 1);

      Point vision = new Point(0, -Y);

      for (int i = 0; i < nAtoms; i++) {
         trans_x[i] = (int)(benzene_x[i] / (1 - benzene_y[i] / Ze));
         trans_y[i] = (int)(Y / (1 - benzene_y[i] / Ze));

         if (i > 5)
            p2.addPoint(trans_x[i], trans_y[i]);
         else
            p.addPoint(trans_x[i], trans_y[i]);

         g.drawString("C", trans_x[i] - d / 2, trans_y[i] - d / 2 - 1);
         g.fillOval(trans_x[i] - d / 2, trans_y[i] - d / 2, d, d);
      }

      if (nAtoms > 6) {
         p2.addPoint(trans_x[2], trans_y[2]);
         p2.addPoint(trans_x[1], trans_y[1]);
      }

      g.drawPolygon(p);

      if (nAtoms > 6) g.drawPolygon(p2);
   }
}

