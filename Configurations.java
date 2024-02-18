import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class Configurations extends Canvas implements MouseListener {

   private final Game parentGame;
   int iConfig = -1;
   int ConfigsNumber;
   int Configs_N_up;
   int Configs_N_down;
   int iHighlighted = -1;
   int nOrbitals = 7;
   int[][] Configs_a;
   int[][] Configs_b;
   int[] Multi_index;
   double U = 0.625;
   double eps[] = { -0.5, -0.379, -0.256, -0.256, -0.009, -0.009, 0.114 };
   int graph[][] = { { 0, 1 } };
   double LowerLimit = -2.5;
   double UpperLimit = 0.0;
   Vector<Double> Diag_H = new Vector<Double>();
   double[][] H;
   int imin;
   int imax;
   boolean isHelpMode = false;
   boolean showOrbitals = false;
   Rectangle[] orbitalsPos; // = new Rectangle [nOrbitals];
   int configDiff_A = 10;
   int configDiff_B = 10;
   Point orbitalPair = new Point();

   Vector<Integer> IR = new Vector<Integer>();
   Vector<Integer> IC = new Vector<Integer>();
   Vector<Double> A = new Vector<Double>();


   public Configurations(int L, int N_up, int N_down, Game itsParent, double[] orbitals, int[][] structure) {
      this.parentGame = itsParent;

      changeModel(L, N_up, N_down, orbitals, structure);

      addMouseListener(this);
   }

   public void changeModel(int L, int N_up, int N_down, double[] orbitals, int[][] structure) {
      eps = orbitals;
      graph = structure;

      nOrbitals = L;
      orbitalsPos = new Rectangle [nOrbitals];

      Combinator spins_up = new Combinator(L, N_up);

      int Na = spins_up.GetConfigs();
      Configs_a = new int [Na][L];
      spins_up.CreateConfigs(Configs_a, 1, 0);

      Combinator spins_down = new Combinator(L, N_down);

      int Nb = spins_down.GetConfigs();
      Configs_b = new int [Nb][L];
      spins_down.CreateConfigs(Configs_b, -1, 0);

      ConfigsNumber = Na * Nb;
      Configs_N_up = Na;
      Configs_N_down = Nb;

      Multi_index = new int [ConfigsNumber];

      double dmin = 0.0;
      double dmax = -10.0;

      imin = 0;
      imax = 0;

      Point initConfig = findInitConfig();

      if (Diag_H.isEmpty() == false) Diag_H.clear();

      HashMap<Integer,Double> diagonals = new HashMap<Integer,Double>();

      int index = 0;
      for (int k1 = Na - 1; k1 > -1; k1--) // (int k1 = 0; k1 < Na; k1++)
      {
         for (int k2 = Nb - 1; k2 > -1; k2--) // (int k2 = 0; k2 < Nb; k2++)
         {
            int M = k1 * Configs_N_down + k2;

            int unpared = 0;
            for (int i = 0; i < nOrbitals; i++)
            {
               unpared += Math.abs(Configs_a[k1][i] + Configs_b[k2][i]);
            }

            if (unpared == 1) {
               if (compareConfigs(initConfig.x, initConfig.y, k1, k2) <= 4) {
                  double Energy = getEnergyOfConfig(k1, k2);
                  if (Energy < dmin) {
                     dmin = Energy;
                     imin = index;
                  }

                  if (Energy > dmax) {
                     dmax = Energy;
                     imax = index;
                  }

                  Multi_index[index] = M;
                  diagonals.put(M, Double.valueOf(Energy));
                  index++;
               }
            }
         }
      }

      LowerLimit = Math.round(dmin);
      if (dmin < LowerLimit) LowerLimit -= 0.5;

      UpperLimit = Math.round(dmax);
      if (dmax > UpperLimit) UpperLimit += 0.5;

      ConfigsNumber = index;

      arrangeConfigs(diagonals);

      for (int i = 0; i < nOrbitals; i++) orbitalsPos[i] = new Rectangle();
   }

   public int compareConfigs(int a1, int b1, int a2, int b2) {
      int configDiff_A = 0;
      int configDiff_B = 0;

      for (int k = 0; k < nOrbitals; k++) {
         configDiff_A += ((Configs_a[a1][k] - Configs_a[a2][k]) != 0) ? 1 : 0;
         configDiff_B += ((Configs_b[b1][k] - Configs_b[b2][k]) != 0) ? 1 : 0;
      }

      return (configDiff_A + configDiff_B);
   }

   public void arrangeConfigs(HashMap<Integer,Double> diagonals) {
      int newIndex[] = new int [ConfigsNumber];

      int M = Multi_index[imin];
      int a1 = M / Configs_N_down;
      int b1 = M % Configs_N_down;

      newIndex[0] = M;

      if (Diag_H.isEmpty() == false) Diag_H.clear();

      Diag_H.addElement((diagonals.get(M)).doubleValue());

      Vector<Integer> newSingles = new Vector<Integer>();
      Vector<Integer> newDoubles = new Vector<Integer>();
      Vector<Integer> newRest = new Vector<Integer>();

      for (int i = 0; i < ConfigsNumber; i++) {
         if (i != imin) {
            M = Multi_index[i];
            int a2 = M / Configs_N_down;
            int b2 = M % Configs_N_down;

            int configDiff_A = 0;
            int configDiff_B = 0;

            int[] VectorA = new int [nOrbitals];
            int[] VectorB = new int [nOrbitals];

            for (int k = 0; k < nOrbitals; k++) {
               VectorA[k] = Configs_a[a1][k] - Configs_a[a2][k];
               VectorB[k] = Configs_b[b1][k] - Configs_b[b2][k];

               configDiff_A += (VectorA[k] != 0) ? 1 : 0;
               configDiff_B += (VectorB[k] != 0) ? 1 : 0;
            }

            if ((configDiff_A + configDiff_B) == 2) {
               newSingles.add(M);
            }
            else if ((configDiff_A + configDiff_B) == 4) {
               newDoubles.add(M);
            }
            else if ((configDiff_A + configDiff_B) > 4) {
               newRest.add(M);
            }
         }
      }

      if ((newSingles.size() + newDoubles.size() + newRest.size() + 1) != ConfigsNumber) {
         System.err.println("Wrong size in arrangeConfigs");
         System.exit(0);
      }

      for (int i = 0; i < newSingles.size(); i++) {
         M = newSingles.get(i);
         newIndex[i+1] = M;
         Diag_H.addElement((diagonals.get(M)).doubleValue());
      }

      for (int i = 0; i < newDoubles.size(); i++) {
         int shift = newSingles.size() + 1;
         M = newDoubles.get(i);
         newIndex[i+shift] = M;
         Diag_H.addElement((diagonals.get(M)).doubleValue());
      }

      for (int i = 0; i < newRest.size(); i++) {
         int shift = newSingles.size() + newDoubles.size() + 1;
         M = newRest.get(i);
         newIndex[i+shift] = M;
         Diag_H.addElement((diagonals.get(M)).doubleValue());
      }

      Multi_index = newIndex;
      imin = 0;

      if (Diag_H.size() != ConfigsNumber) {
         System.err.println("Wrong size in arrangeConfigs");
         System.exit(0);
      }
   }

   public Point findInitConfig() {
      int[] Array_a = new int [nOrbitals];
      int[] Array_b = new int [nOrbitals];

      int Na = Configs_N_up;
      int Nb = Configs_N_down;

      int Array_11_a[]  = { 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0 };
      int Array_11_b[] = { 0,-1,-1,-1,-1,-1, 0, 0, 0, 0, 0 };

      int Array_7_a[] = { 1, 1, 1, 1, 0, 0, 0 };
      int Array_7_b[] = { 0,-1,-1,-1, 0, 0, 0 };

      if (nOrbitals == 11) {
         Array_a = Array_11_a;
         Array_b = Array_11_b;
      }
      else if (nOrbitals == 7) {
         Array_a = Array_7_a;
         Array_b = Array_7_b;
      }

      for (int k1 = Na - 1; k1 > -1; k1--) {
         for (int k2 = Nb - 1; k2 > -1; k2--) {
            int configDiff_A = 0;
            int configDiff_B = 0;

            for (int k = 0; k < nOrbitals; k++) {
               configDiff_A += ((Array_a[k] - Configs_a[k1][k]) != 0) ? 1 : 0;
               configDiff_B += ((Array_b[k] - Configs_b[k2][k]) != 0) ? 1 : 0;
            }

            if ((configDiff_A + configDiff_B) == 0) {
               return new Point(k1, k2);
            }
         }
      }

      System.err.println("There is no the initial config in the basis set");
      System.exit(0);

      return new Point(0, 0);
   }

   public void mouseClicked(MouseEvent e) {
      if (iHighlighted != -1) {
         if (e.getButton() == MouseEvent.BUTTON1) {
            for (int i = 0; i < nOrbitals; i++) {
               if (orbitalsPos[i].contains(e.getPoint().x, e.getPoint().y)) {
                  parentGame.notifyEvent("Configurations event", i + 1);
                  break;
               }
            }
         }
      }
   }

   public void mousePressed(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
      showOrbitals = true;

      if (iHighlighted != -1) repaint();
   }

   public void mouseExited(MouseEvent e) {
      showOrbitals = false;

      if (iHighlighted != -1) repaint();
   }

   public double getMatrixElementOfH(int i, int j) {
      if (i == j) {
         return (Diag_H.isEmpty() == false) ? Diag_H.get(i-1) : 0.0;
      }
      else {
         if (A.isEmpty() == false) {
            for (int ii = 0; ii < A.size(); ii++) {
               int ir = IR.get(ii);
               int ic = IC.get(ii);

               if ((i-1 == ir && j-1 == ic) || (j-1 == ir && i-1 == ic)) return A.get(ii);
            }
         }
      }

      return 0.0;
   }

   public double xHx(int[] WF) {
      double Result = 0.0;

      if (Diag_H.isEmpty() == false) {
         for (int i = 0; i < ConfigsNumber; i++) {
            Result += WF[i] * Diag_H.get(i) * WF[i];
         }
      }

      if (A.isEmpty() == false) {
         for (int ii = 0; ii < A.size(); ii++) {
            int ir = IR.get(ii);
            int ic = IC.get(ii);

            Result += WF[ir] * A.get(ii) * WF[ic] * 2; // because A is symmetric
         }
      }

      return Result;
   }

   public void printConfig(int a2, int b2) {
      for (int k = 0; k < nOrbitals; k++) System.out.print(Configs_a[a2][k] + ", ");
      System.out.println("");

      for (int k = 0; k < nOrbitals; k++) System.out.print(Configs_b[b2][k] + ", ");
      System.out.println("");
   }

   public double constructHamiltonian(double[] V_int) {
      int index = 0;

      if (IR.isEmpty() == false) IR.clear();
      if (IC.isEmpty() == false) IC.clear();
      if (A.isEmpty() == false) A.clear();

      Vector<Integer> subSpace = new Vector<Integer>();

      subSpace.addElement(imin);

      for (int i = 0; i < ConfigsNumber; i++) {
         int L = Multi_index[i];
         int a1 = L / Configs_N_down;
         int b1 = L % Configs_N_down;

         for (int j = i + 1; j < ConfigsNumber; j++) {
            int R = Multi_index[j];
            int a2 = R / Configs_N_down;
            int b2 = R % Configs_N_down;

            double h = Hamiltoninan(a1, a2, b1, b2, V_int);

            {
               if (h != 0.0) {
                  if (i == imin) index = j;

                  IR.addElement(i);
                  IC.addElement(j);
                  A.addElement(h);

                  if (!subSpace.contains(i)) subSpace.addElement(i);
                  if (!subSpace.contains(j)) subSpace.addElement(j);
               }
            }
         }
      }


      double Hsmall [][] = new double [subSpace.size()][subSpace.size()];

      for (int i = 0; i < Hsmall.length; i++) {
         for (int j = 0; j < Hsmall[i].length; j++) {
            Hsmall[i][j] = 0.0;
         }
      }

      Hsmall[0][0] = Diag_H.get(imin); // H[imin][imin];

      for (int ii = 0; ii < A.size(); ii++) {
         int ir = IR.get(ii);
         int ic = IC.get(ii);
         double h = A.get(ii);

         int i = subSpace.indexOf(ir);
         int j = subSpace.indexOf(ic);

         Hsmall[i][i] = Diag_H.get(ir);
         Hsmall[j][j] = Diag_H.get(ic);

         Hsmall[i][j] = h;
         Hsmall[j][i] = h;
      }

      double H11 = Diag_H.get(imin); // H[imin][imin];
      double H22 = Diag_H.get(index); // H[index][index];
      double H12 = Hsmall[0][1]; // H[imin][index];

      double a = 1.0;
      double b = -(H11 + H22);
      double c = H11 * H22 - H12 * H12;

      double x1 = (-b + Math.sqrt(b * b - 4 * a * c)) / (2 * a);
      double x2 = (-b - Math.sqrt(b * b - 4 * a * c)) / (2 * a);

      double x = Math.min(x1, x2);

      System.out.println("Exact solution: E = " + x);

      H11 = H11 - x;
      H22 = H22 - x;

      double c1 = H22 / Math.sqrt(H12 * H12 + H22 * H22);
      double c2 = -H12 / Math.sqrt(H12 * H12 + H22 * H22);

      System.out.println("Exact WF: (" + c1 + ", " + c2 + ")");

      double Hcopy [][] = new double [subSpace.size()][subSpace.size()];

      for (int i = 0; i < Hcopy.length; i++) {
         for (int j = 0; j < Hcopy[i].length; j++) {
            Hcopy[i][j] = Hsmall[i][j];
         }
      }

      double [] d = new double[Hsmall.length + 1];
      double [] e = new double[Hsmall.length + 1];

      LinearAlgebra.tred2(Hsmall, Hsmall.length, d, e);

      LinearAlgebra.tqli(d, e, Hsmall.length, Hsmall);

      LinearAlgebra.sort(Hsmall.length, 1, d, Hsmall);

      for (int i = 0; i < d.length - 1; i++)
         System.out.println("" + d[i]);

      double eigenval = 0.0;

      int ieig = 0;

      for (int i = 0; i < Hsmall.length; i++) {
         for (int j = 0; j < Hsmall[i].length; j++) {
            eigenval += Hsmall[i][ieig] * Hcopy[i][j] * Hsmall[j][ieig];
         }
      }

      System.out.println("Min eigenvalue : " + eigenval);

      System.out.println("Corresponding eigenvector :");

      for (int j = 0; j < Hsmall.length; j++)
         System.out.print("" + Hsmall[j][ieig] + ", ");

      System.out.println("");

      return d[0]; // x;
   }

   private double Hamiltoninan(int a1, int a2, int b1, int b2, double[] V_int) {
      double Result = 0.0;

      int SummaA = 0;
      int SummaB = 0;

      int[] VectorA = new int [nOrbitals];
      int[] VectorB = new int [nOrbitals];

      for (int i = 0; i < nOrbitals; i++) {
         VectorA[i] = Configs_a[a1][i] - Configs_a[a2][i];
         VectorB[i] = Configs_b[b1][i] - Configs_b[b2][i];
      }

      for (int i = 0; i < nOrbitals; i++) {
         SummaA += (VectorA[i] != 0) ? 1 : 0;
         SummaB += (VectorB[i] != 0) ? 1 : 0;
      }

      if (SummaA == 2 && SummaB == 0) {
         for (int ii = 0; ii < graph.length; ii++) {
            int i = graph[ii][0];
            int j = graph[ii][1];

            if (VectorA[i] != 0 &&
                Configs_a[a1][i] == Configs_a[a2][j] &&
                Configs_a[a2][i] == Configs_a[a1][j]) {
               double V = V_int[ii];

               int na = 0;
               for (int k = i + 1; k < j; k++) na += Configs_a[a1][k];

               int nb = 0;
               for (int k = i; k < j; k++) nb += Configs_b[b1][k];

               int sign = ((na - nb) % 2 == 0) ? 1 : -1;

               Result += sign * V;
            }
         }
      }
      else if (SummaA == 0 && SummaB == 2) {
         for (int ii = 0; ii < graph.length; ii++) {
            int i = graph[ii][0];
            int j = graph[ii][1];

            if (VectorB[i] != 0 &&
                Configs_b[b1][i] == Configs_b[b2][j] &&
                Configs_b[b2][i] == Configs_b[b1][j]) {
               double V = V_int[ii];

               int na = 0;
               for (int k = i + 1; k <= j; k++) na += Configs_a[a1][k];

               int nb = 0;
               for (int k = i + 1; k < j; k++) nb += Configs_b[b1][k];

               int sign = ((na - nb) % 2 == 0) ? 1 : -1;

               Result += sign * V;
            }
         }
      }

      return Result;
   }

   public void disableConfig() {
      iConfig = -1;
   }

   public void updateConfigDiff() {
      if (iHighlighted != -1) {
         int M = Multi_index[0];
         int a1 = M / Configs_N_down;
         int b1 = M % Configs_N_down;

         M = Multi_index[iConfig];
         int a2 = M / Configs_N_down;
         int b2 = M % Configs_N_down;

         configDiff_A = 0;
         configDiff_B = 0;

         int[] VectorA = new int [nOrbitals];
         int[] VectorB = new int [nOrbitals];

         for (int k = 0; k < nOrbitals; k++) {
            VectorA[k] = Configs_a[a1][k] - Configs_a[a2][k];
            VectorB[k] = Configs_b[b1][k] - Configs_b[b2][k];

            configDiff_A += (VectorA[k] != 0) ? 1 : 0;
            configDiff_B += (VectorB[k] != 0) ? 1 : 0;
         }

         if (configDiff_A == 2 && configDiff_B == 0) {
            for (int i = 0; i < nOrbitals; i++) {
               for (int j = i + 1; j < nOrbitals; j++) {
                  if (VectorA[i] != 0 && VectorA[j] != 0) {
                     orbitalPair.x = i;
                     orbitalPair.y = j;
                  }
               }
            }
         }
         else if (configDiff_A == 0 && configDiff_B == 2) {
            for (int i = 0; i < nOrbitals; i++) {
               for (int j = i + 1; j < nOrbitals; j++) {
                  if (VectorB[i] != 0 && VectorB[j] != 0) {
                     orbitalPair.x = i;
                     orbitalPair.y = j;
                  }
               }
            }
         }

      }
   }

   public int setConfig(int i) {
      --i;

      if (i >= 0 && i < ConfigsNumber)
      {
         iConfig = i;
      }

      if (i < 0) iConfig = 0;
      if (i >= ConfigsNumber) iConfig = ConfigsNumber - 1;

      updateConfigDiff();

      return (iConfig + 1);
   }

   public int getConfig() {
      return (iConfig + 1);
   }

   public int getConfigsNumber() {
      return ConfigsNumber;
   }

   public double getUpperLimit() {
      return UpperLimit;
   }

   public double getLowerLimit() {
      return LowerLimit;
   }

   public int Increase() {
      iConfig++;
      if (iConfig == ConfigsNumber) iConfig = 0;

      updateConfigDiff();

      return (iConfig + 1);
   }

   public int Decrease() {
      iConfig--;
      if (iConfig == -1) iConfig = ConfigsNumber - 1;

      updateConfigDiff();

      return (iConfig + 1);
   }

   public int setHighlighted(int i) {
      --i;

      if (i >= 0 && i < nOrbitals)
      {
         iHighlighted = i;
      }

      if (i < 0) iHighlighted = 0;
      if (i >= nOrbitals) iHighlighted = nOrbitals - 1;

      return (iHighlighted + 1);
   }

   public void setHelpMode(boolean mode) {
      isHelpMode = mode;
   }

   private void fillOrbital(Graphics g, int x1, int y1, int x2, int y2, int spin_a, int spin_b) {
      int length = Math.abs(x1 - x2);
      int Xa = (spin_b == 0) ? (x2 - x1) / 2 + x1 : (x2 - x1) / 4 + x1;
      int Xb = (spin_a == 0) ? (x2 - x1) / 2 + x1 : 3 * (x2 - x1) / 4 + x1;
      int Y1 = y1 + length / 2;
      int Y2 = y1 - length / 2;
      int shift = length / 5;

      if (spin_a == 1)
      {
         g.drawLine(Xa, Y1, Xa, Y2);
         g.drawLine(Xa, Y2, Xa - shift, Y2 + shift);
         g.drawLine(Xa, Y2, Xa + shift, Y2 + shift);
      }

      if (spin_b == -1)
      {
         g.drawLine(Xb, Y1, Xb, Y2);
         g.drawLine(Xb, Y1, Xb + shift, Y1 - shift);
         g.drawLine(Xb, Y1, Xb - shift, Y1 - shift);
      }
   }

   private double getEnergyOfConfig(int ia, int ib) {
      double Energy = 0.0;

      for (int i = 0; i < nOrbitals; i++) {
         Energy += eps[i] * (Math.abs(Configs_a[ia][i]) + Math.abs(Configs_b[ib][i]));
      }

      if (Math.abs(Configs_a[ia][0]) != 0 && Math.abs(Configs_b[ib][0]) != 0) {
      }

      Energy += U * Math.abs(Configs_a[ia][0]) * Math.abs(Configs_b[ib][0]);

      return Energy;
   }

   public double getEnergyOfConfig(int k) {
      --k;

      if (k < 0 || k >= ConfigsNumber) return 0.0;

      int M = Multi_index[k];
      int ia = M / Configs_N_down;
      int ib = M % Configs_N_down;

      double Energy = 0.0;

      for (int i = 0; i < nOrbitals; i++) {
         Energy += eps[i] * (Math.abs(Configs_a[ia][i]) + Math.abs(Configs_b[ib][i]));
      }

      if (Math.abs(Configs_a[ia][0]) != 0 && Math.abs(Configs_b[ib][0]) != 0) {
//         System.out.println("n * U = " + Math.abs(Configs_a[ia][0]) * Math.abs(Configs_b[ib][0]));
//         System.out.println("U = " + U);
      }

      Energy += U * Math.abs(Configs_a[ia][0]) * Math.abs(Configs_b[ib][0]);

      return Energy;
   }

   private void drawEnergyScale(Graphics g, int width, int height,
                                double Energy) {
      int length = Math.min(width, height) / 24;
      int shift = Math.min(width, height) / 9;
      int dx = 5;

      width = width / 2;

      String s1 = "" + LowerLimit;
      String s2 = "" + UpperLimit;

      Font font = new Font("Serif", Font.BOLD, 3 * length / 2); // 20);
      g.setFont(font);

      FontMetrics metrics = g.getFontMetrics(font);

      int x = Math.max(metrics.stringWidth(s1), metrics.stringWidth(s2)) + dx + 3;
      int y1 = height - shift / 2;
      int y2 = shift / 2;

      g.setColor(Color.black);

      g.drawLine(x, y1, x, y2);
      g.drawLine(x, y2, x - length / 3, y2 + length / 2);
      g.drawLine(x, y2, x + length / 3, y2 + length / 2);

      Energy = Math.round(Energy * 1000.0) / 1000.0;

      String E = "Energy of |" + (iConfig + 1) + "> = " + Energy;

      g.setColor(Color.blue);

      g.drawString(E, x + 2 * length / 2, y2);

      int y3 = (int)((y1 - 2*y2) * (Energy - UpperLimit) / (LowerLimit - UpperLimit));

      g.drawLine(x - length / 2, y3 + 2*y2, x + length / 2, y3 + 2*y2);

      g.setColor(Color.black);

      g.drawString(s1, x - metrics.stringWidth(s1) - dx, y1);

      g.drawString(s2, x - metrics.stringWidth(s2) - dx, 2 * y2);
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
      if (iConfig == -1) return;

      int width = getSize().width;
      int height = getSize().height;
      int length = Math.min(width, height) / 24;
      int shift = Math.min(width, height) / 9;
      int dx = width / 3;

      width = width / 2;

      if (isHelpMode) {
         Color oldColor = g.getColor();
         g.setColor(Color.black);

         int d2x = 2;

         if (iConfig == 0) {
            String text[] = {"This is the initial",
                             "configuration.",
                             "It should be in",
                             "the wave function."};

            FontMetrics fm = g.getFontMetrics();
            int h = height / (text.length + 1);

            int x, y;
            for (int i = 0; i < text.length; i++) {
               x = (width - fm.stringWidth(text[i])) / 2 + d2x;
               y = (i + 1) * h + fm.getHeight() / 2 - 1;
               g.drawString (text[i], x, y);
            }
         }
         else if ((configDiff_A + configDiff_B) == 2) {
            if (orbitalPair.x == 0 || orbitalPair.y == 0) {
               String text[] = {"It's a single excitation",
                                "from the initial state.",
                                "Check the symmetry",
                                "of orbitals involved",
                                "in this transition."};

               FontMetrics fm = g.getFontMetrics();
               int h = height / (text.length + 1);

               int x, y;
               for (int i = 0; i < text.length; i++) {
                  x = (width - fm.stringWidth(text[i])) / 2 + d2x;
                  y = (i + 1) * h + fm.getHeight() / 2 - 1;
                  g.drawString (text[i], x, y);
               }
            }
            else {
               String text[] = {"It's a single excitation",
                                "from the initial state.",
                                "However, it doesn't",
                                "involve the hydrogen",
                                "orbital."};

               FontMetrics fm = g.getFontMetrics();
               int h = height / (text.length + 1);

               int x, y;
               for (int i = 0; i < text.length; i++) {
                  x = (width - fm.stringWidth(text[i])) / 2 + d2x;
                  y = (i + 1) * h + fm.getHeight() / 2 - 1;
                  g.drawString (text[i], x, y);
               }
            }

            int i = orbitalPair.x;
            int j = orbitalPair.y;

            int x = orbitalsPos[i].x + orbitalsPos[i].width / 2;
            int y = orbitalsPos[i].y + orbitalsPos[i].height / 2;

            int x2 = orbitalsPos[j].x + orbitalsPos[j].width / 2;
            int y2 = orbitalsPos[j].y + orbitalsPos[j].height / 2;

            g.drawLine(x, y, x2, y2);

            if (iHighlighted != -1 && showOrbitals) {
               Color newColor = g.getColor();
               g.setColor(Color.white);

               g.fillRect(orbitalsPos[i].x, orbitalsPos[i].y, orbitalsPos[i].width, orbitalsPos[i].height);
               g.fillRect(orbitalsPos[j].x, orbitalsPos[j].y, orbitalsPos[j].width, orbitalsPos[j].height);

               g.setColor(newColor);
            }
         }
         else if ((configDiff_A + configDiff_B) == 4) {
            String text[] = {"It is a double",
                             "excitation of the",
                             "initial state."};

            FontMetrics fm = g.getFontMetrics();
            int h = height / (text.length + 1);

            int x, y;
            for (int i = 0; i < text.length; i++) {
               x = (width - fm.stringWidth(text[i])) / 2 + d2x;
               y = (i + 1) * h + fm.getHeight() / 2 - 1;
               g.drawString (text[i], x, y);
            }
         }
         else if ((configDiff_A + configDiff_B) > 4) {
            String text[] = {"This configuration",
                             "corresponds to more",
                             "than two electron",
                             "promotions to the",
                             "unoccupied orbitals."};

            FontMetrics fm = g.getFontMetrics();
            int h = height / (text.length + 1);

            int x, y;
            for (int i = 0; i < text.length; i++) {
               x = (width - fm.stringWidth(text[i])) / 2 + d2x;
               y = (i + 1) * h + fm.getHeight() / 2 - 1;
               g.drawString (text[i], x, y);
            }
         }

         g.setColor(oldColor);
      }

      int M = Multi_index[iConfig];
      int ia = M / Configs_N_down;
      int ib = M % Configs_N_down;

      Graphics2D g2d = (Graphics2D)g;
      g2d.setStroke(new BasicStroke(2));

      double Energy = getEnergyOfConfig(ia, ib);

      drawEnergyScale(g, width * 2, height, Energy);

      int[] X1 = new int [nOrbitals];
      int[] X2 = new int [nOrbitals];
      int[] Y = new int [nOrbitals];

      X1[0] = (width - 0*dx) / 2 - length;
      X2[0] = (width - 0*dx) / 2 + length;
      Y[0] = height - shift / 2;

      if (nOrbitals == 7) {
         X1[1] = width / 2 - length + width;
         X2[1] = width / 2 + length + width;
         Y[1] = height - shift;

         X1[2] = (width - dx) / 2 - length + width;
         X2[2] = (width - dx) / 2 + length + width;
         Y[2] = 2 * height / 3;

         X1[3] = (width + dx) / 2 - length + width;
         X2[3] = (width + dx) / 2 + length + width;
         Y[3] = 2 * height / 3;

         X1[4] = (width - dx) / 2 - length + width;
         X2[4] = (width - dx) / 2 + length + width;
         Y[4] = height / 3;

         X1[5] = (width + dx) / 2 - length + width;
         X2[5] = (width + dx) / 2 + length + width;
         Y[5] = height / 3;

         X1[6] = width / 2 - length + width;
         X2[6] = width / 2 + length + width;
         Y[6] = shift;
      }
      else if (nOrbitals == 11) {
         int bottom = height - shift;
         int top = shift;

         for (int i = 1; i < nOrbitals; i++) {
            X1[i] = width / 2 - length + width;
            X2[i] = width / 2 + length + width;

            if (i != 1 && i != 10) {
               if (i % 2 == 0) {
                  X1[i] += dx / 5; // 4;
                  X2[i] += dx / 5; // 4;
               }
               else {
                  X1[i] -= dx / 5; // 4;
                  X2[i] -= dx / 5; // 4;
               }
            }

            int y = (int)((bottom - top) * (eps[i] - eps[10]) / (eps[1] - eps[10]));
            Y[i] = y + top;
         }
      }

      for (int i = 0; i < nOrbitals; i++)
      {
         g.drawLine(X1[i], Y[i], X2[i], Y[i]);

         fillOrbital(g, X1[i], Y[i], X2[i], Y[i], Configs_a[ia][i], Configs_b[ib][i]);

         int x = X1[i] - shift / 2;
         int y = Y[i] - (length + shift / 6);
         int w = 2 * (length + shift / 2);
         int h = 2 * (length + shift / 6);

         orbitalsPos[i].setRect(x, y, w, h);
      }

      if (iHighlighted != -1) {
         int x = X1[iHighlighted] - shift / 2;
         int y = Y[iHighlighted] - (length + shift / 6);
         int w = 2 * (length + shift / 2);
         int h = 2 * (length + shift / 6);

         g.setColor(Color.red);
         g.drawOval(x, y, w, h);
      }

      if (iHighlighted != -1 && showOrbitals) {
         g.setColor(Color.gray);
         g2d.setStroke(new BasicStroke(1));

         for (int i = 0; i < nOrbitals; i++) {
            g.drawRect(orbitalsPos[i].x, orbitalsPos[i].y, orbitalsPos[i].width, orbitalsPos[i].height);
         }
      }
   }
}

