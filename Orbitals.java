import java.awt.*;

public class Orbitals extends Canvas {

   int OrbitalsNumber = 7;
   int iOrbital = 5;
   boolean showHelp = false;
   double eps[] = { -0.5, -0.379, -0.256, -0.256, -0.009, -0.009, 0.114 };

   double MO[][] = new double [OrbitalsNumber-1][OrbitalsNumber-1];

   double MO7[][] = {{ 0.41, 0.41, 0.41, 0.41, 0.41, 0.41 },
                     {-0.58,-0.29, 0.29, 0.58, 0.29,-0.29 },
                     { 0.00,-0.50,-0.50, 0.00, 0.50, 0.50 },
                     { 0.58,-0.29,-0.29, 0.58,-0.29,-0.29 },
                     { 0.00, 0.50,-0.50, 0.00, 0.50,-0.50 },
                     {-0.41, 0.41,-0.41, 0.41,-0.41, 0.41 }};

   double MO11[][] = {{ 0.30, 0.46, 0.46, 0.30, 0.23, 0.23, 0.30, 0.23, 0.23, 0.30 },
                      { 0.26, 0.00, 0.00, 0.26, 0.43, 0.43,-0.26,-0.43,-0.43,-0.26 },
                      {-0.40,-0.35, 0.35, 0.40, 0.17,-0.17,-0.40,-0.17, 0.17, 0.40 },
                      { 0.00, 0.41, 0.41, 0.00,-0.41,-0.41, 0.00,-0.41,-0.41, 0.00 },
                      {-0.43, 0.00, 0.00, 0.43, 0.26,-0.26, 0.43, 0.26,-0.26,-0.43 },
                      {-0.43, 0.00, 0.00,-0.43, 0.26, 0.26, 0.43,-0.26,-0.26, 0.43 },
                      { 0.00,-0.41, 0.41, 0.00,-0.41, 0.41, 0.00, 0.41,-0.41, 0.00 },
                      { 0.40,-0.35,-0.35, 0.40,-0.17,-0.17, 0.40,-0.17,-0.17, 0.40 },
                      {-0.26, 0.00, 0.00, 0.26,-0.43, 0.43, 0.26,-0.43, 0.43,-0.26 },
                      { 0.30,-0.46, 0.46,-0.30, 0.23,-0.23, 0.30,-0.23, 0.23,-0.30 }};


   public Orbitals(int i, int L, double[] orbitals) {
      eps = orbitals;

      OrbitalsNumber = L;

      --i;

      if (i >= 0 && i < OrbitalsNumber)
      {
         iOrbital = i;
      }

      if (OrbitalsNumber == 7)
         MO = MO7;
      else
         MO = MO11;
   }

   public void changeModel(int i, int L, double[] orbitals) {
      eps = orbitals;

      OrbitalsNumber = L;

      --i;

      if (i >= 0 && i < OrbitalsNumber)
      {
         iOrbital = i;
      }

      if (OrbitalsNumber == 7)
         MO = MO7;
      else
         MO = MO11;
   }

   public int setOrbital(int i) {
      --i;

      if (i >= 0 && i < OrbitalsNumber)
      {
         iOrbital = i;
      }

      if (i < 0) iOrbital = 0;
      if (i >= OrbitalsNumber) iOrbital = OrbitalsNumber - 1;

      return (iOrbital + 1);
   }

   public int getOrbitalsNumber() {
      return OrbitalsNumber;
   }

   public int Next() {
      iOrbital++;
      if (iOrbital == OrbitalsNumber) iOrbital = 0;
      return (iOrbital + 1);
   }

   public int Previous() {
      iOrbital--;
      if (iOrbital == -1) iOrbital = OrbitalsNumber - 1;
      return (iOrbital + 1);
   }

   public void setHelpMode(boolean status) {
      showHelp = status;
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
      gr.setColor(Color.white);
      gr.fillRect(0, 0, getSize().width, getSize().height);
      gr.setColor(oldColor);

      paint(gr); 
      g.drawImage(offScreenBuffer, 0, 0, this);     
   }

   public void paint(Graphics g) {
      int width = getSize().width;
      int height = getSize().height;
      int length = Math.min(width, height) / 24;
      int r1 = 5 * Math.min(width, height) / 18;
      int R = (OrbitalsNumber > 7) ? Math.min(width, height) / 4 : Math.min(width, height) / 3;

      g.translate(width / 2, height / 2);

      int[] x = new int [OrbitalsNumber];
      int[] y = new int [OrbitalsNumber];
      int[] r = new int [OrbitalsNumber];

      Color[] color = new Color [OrbitalsNumber];

      String[] atoms = new String [OrbitalsNumber];
      String[] Dnh_labels = new String [OrbitalsNumber];
      String[] Cnv_labels = new String [OrbitalsNumber];

      atoms[0] = "H";
      for (int i = 1; i < OrbitalsNumber; i++) {
         atoms[i] = "C";
      }

      if (OrbitalsNumber == 7) {
         Dnh_labels[0] = "1s";
         Dnh_labels[1] = "A.2u";
         Dnh_labels[2] = "E.1g";
         Dnh_labels[3] = "E.1g";
         Dnh_labels[4] = "E.2u";
         Dnh_labels[5] = "E.2u";
         Dnh_labels[6] = "B.2g";
      }
      else {
         Dnh_labels[0]  = "1s";
         Dnh_labels[1]  = "B.1u";
         Dnh_labels[2]  = "B.2g";
         Dnh_labels[3]  = "B.3g";
         Dnh_labels[4]  = "B.1u";
         Dnh_labels[5]  = "A.u";
         Dnh_labels[6]  = "B.2g";
         Dnh_labels[7]  = "B.3g";
         Dnh_labels[8]  = "B.1u";
         Dnh_labels[9]  = "A.u";
         Dnh_labels[10] = "B.3g";
      }

      if (OrbitalsNumber == 7) {
         Cnv_labels[0] = "A1";
         Cnv_labels[1] = "A1";
         Cnv_labels[2] = "E1";
         Cnv_labels[3] = "E1";
         Cnv_labels[4] = "E2";
         Cnv_labels[5] = "E2";
         Cnv_labels[6] = "B2";
      }
      else {
         Cnv_labels[0]  = "A1";
         Cnv_labels[1]  = "A1";
         Cnv_labels[2]  = "B1";
         Cnv_labels[3]  = "B2";
         Cnv_labels[4]  = "A1";
         Cnv_labels[5]  = "A2";
         Cnv_labels[6]  = "B1";
         Cnv_labels[7]  = "B2";
         Cnv_labels[8]  = "A1";
         Cnv_labels[9]  = "A2";
         Cnv_labels[10] = "B2";
      }

      Color color_plus = Color.blue;
      Color color_minus = Color.red;

      if (iOrbital == 0) {
         r[0] = (int)(0.35 * r1);  color[0] = color_plus;

         for (int i = 1; i < OrbitalsNumber; i++) {
            r[i] = 0;
            color[i] = color_plus;
         }
      }
      else {
         r[0] = 0;  color[0] = color_plus;

         for (int i = 1; i < OrbitalsNumber; i++) {
            r[i] = (int)Math.abs(MO[iOrbital-1][i-1] * r1);
            color[i] = (MO[iOrbital-1][i-1] > 0) ? color_plus : color_minus;
         }
      }

      Polygon p = new Polygon();
      Polygon p2 = new Polygon();

      x[0] = 0;
      y[0] = 0;

      int shift = (OrbitalsNumber > 7) ? (int)(R * Math.cos(Math.PI / 6)) : 0;

      for (int i = 1; i < OrbitalsNumber; i++) {
         x[i] = (int)(R * Math.cos((2 * i + 1) * Math.PI / 6)) + shift;
         y[i] = (int)(R * Math.sin((2 * i + 1) * Math.PI / 6));

         if (i > 6) {
            x[i] -= (int)(2 * R * Math.cos(Math.PI / 6));
         }

         if (i > 6)
            p2.addPoint(x[i], y[i]);
         else
            p.addPoint(x[i], y[i]);
      }

      if (OrbitalsNumber > 7) {
         p2.addPoint(x[3], y[3]);
         p2.addPoint(x[2], y[2]);
      }


      Font font = new Font("Helvetica", Font.PLAIN, 2 * length / 1);
      g.setFont(font);

      g.setColor(Color.black);
      g.drawPolygon(p);
      if (OrbitalsNumber > 7) g.drawPolygon(p2);

      FontMetrics metrics = g.getFontMetrics(font);
      int sh = metrics.getAscent();

      g.drawString(Dnh_labels[iOrbital], length - width / 2, sh + length - height / 2);

      if (showHelp) {
         int sw = metrics.stringWidth(Cnv_labels[iOrbital]);

         int n = 3;
         int triangle_x[] = new int [n];
         int triangle_y[] = new int [n];

         if (OrbitalsNumber == 7) {
            triangle_x[0] = length - width / 2 + sw / 2;
            triangle_x[1] = length - width / 2 + sw / 2 - length / 3;
            triangle_x[2] = length - width / 2 + sw / 2 + length / 3;

            triangle_y[0] = sh / 2 - sh;
            triangle_y[1] = sh / 2 - sh - length;
            triangle_y[2] = sh / 2 - sh - length;

            g.drawLine(length - width / 2 + sw / 2, sh + length - height / 2 + 5, 
                       length - width / 2 + sw / 2, sh / 2 - sh);

            g.fillPolygon(triangle_x, triangle_y, n);

            g.drawString(Cnv_labels[iOrbital], length - width / 2, sh / 2);
         }
         else {
            int sw2 = metrics.stringWidth(Dnh_labels[iOrbital]);

            triangle_x[0] = -2 * sw / 3;
            triangle_x[1] = -2 * sw / 3 - length;
            triangle_x[2] = -2 * sw / 3 - length;

            triangle_y[0] = sh + length - height / 2 - 5;
            triangle_y[1] = sh + length - height / 2 - 5 - length / 3;
            triangle_y[2] = sh + length - height / 2 - 5 + length / 3;

            g.drawLine(length - width / 2 + sw2 + 3, sh + length - height / 2 - 5, 
                       -2 * sw / 3, sh + length - height / 2 - 5);

            g.fillPolygon(triangle_x, triangle_y, n);

            g.drawString(Cnv_labels[iOrbital], -sw / 2, sh + length - height / 2);
         }
      }

      g.drawString("E = " + eps[iOrbital], length - width / 2, height / 2 - length);

      for (int i = 0; i < x.length; i++)
      {
         g.setColor(Color.white);
         g.fillOval(x[i] - sh / 2, y[i] - sh / 2, sh, sh);

         g.setColor(color[i]);
         g.fillOval(x[i] - r[i], y[i] - r[i], 2 * r[i], 2 * r[i]);

         int sw = metrics.stringWidth(atoms[i]);

         g.setColor(Color.black);

         g.drawString(atoms[i], x[i] - sw / 2 - 1, y[i] + sh / 2 - 1);
      }
   }
}