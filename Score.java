import java.awt.*;
import java.util.*;
import java.awt.event.*;

public class Score extends Canvas implements MouseListener {

   private final Configurations CI;
   private final TextField matrix_element;

   Hashtable<Integer,Integer> result_WF = new Hashtable<Integer,Integer>();
   Hashtable<Integer,Double> result_E = new Hashtable<Integer,Double>();

   Vector<Rectangle> config_pos = new Vector<Rectangle>();
   Vector<Integer> config_val = new Vector<Integer>();
   Vector<Double> setOfEnergies = new Vector<Double>();

   boolean showEnergy = true;
   boolean showBold = false;
   boolean isGameOver = false;
   boolean isGameStarted = false;

   int LeftConfig = -1;
   int RightConfig = -1;

   int iHighlighted = -1;

   double exact_E;


   public Score(Configurations Ci, TextField matrix_element, double[] integrals) {
      this.CI = Ci;
      exact_E = this.CI.constructHamiltonian(integrals);
      this.matrix_element = matrix_element;
      addMouseListener(this);
   }

   public void changeModel(double[] integrals) {
      exact_E = CI.constructHamiltonian(integrals);

      result_WF.clear();
      result_E.clear();
      setOfEnergies.clear();
      RightConfig = -1;
   }

   public boolean copyHashtables(Hashtable<Integer,Integer> table1, 
                                 Hashtable<Integer,Double> table2, int new_config) {
      result_WF.putAll(table1);
      result_E.putAll(table2);

      setLeftConfig(new_config);


      setRightConfig(new_config);

      iHighlighted = -1;

      updateMatrixElement();


      double E1 = calculateEnergy();

      int[] WF = calculateWaveFunction(CI.getConfigsNumber());

      double norm = 0.0;

      for (int i = 0; i < WF.length; i++) {
         norm += WF[i] * WF[i];
      }

      double E2 = CI.xHx(WF) / norm;
      setOfEnergies.add(E2);

      if ((Math.round(E2 * 1000.0) / 1000.0) == (Math.round(exact_E * 1000.0) / 1000.0))
         return true;
      else
         return false;
   }

   public void setGameOver(boolean gameStatus) {
      isGameOver = gameStatus;
      isGameStarted = !gameStatus;
   }

   public void setLeftConfig(int new_config) {
      LeftConfig = new_config;
   }

   public void setRightConfig(int new_config) {
      RightConfig = new_config;
   }

   public void updateMatrixElement() {
      if (RightConfig != -1) {
         CI.setConfig(RightConfig);
         CI.repaint();
      }
      repaint();
      if (LeftConfig != -1 && RightConfig != -1) {
         String s = "<" + LeftConfig + "|H|" + RightConfig + ">=";
         double LHR = CI.getMatrixElementOfH(LeftConfig, RightConfig);
         s = s + Math.round(LHR * 1000.0) / 1000.0;
         matrix_element.setText(s);
      }
   }

   public void clearResults() {
      result_WF.clear();
      result_E.clear();
      setOfEnergies.clear();
      isGameOver = false;
      isGameStarted = true;
      RightConfig = -1;
   }

   public void setChoice(boolean choice) {
      showEnergy = choice;
   }

   private double calculateEnergy() {
      Set set = result_WF.entrySet();
      Iterator it = set.iterator();

      double norm = 0.0;
      double averageEnergy = 0.0;
      int counter = 0;

      while(it.hasNext()) { 
         Map.Entry me = (Map.Entry)it.next();
         int Ci = ((Integer)me.getKey()).intValue();
         double value = ((Integer)me.getValue()).doubleValue();
         norm += value * value;

         Double E = (Double)result_E.get(Ci);

         if (E != null) {
            averageEnergy += E.doubleValue() * value * value;
         }

         counter++;
      }

      return (averageEnergy / norm);
   }

   private int[] calculateWaveFunction(int size) {
      int[] WF = new int [size];

      Set set = result_WF.entrySet();
      Iterator it = set.iterator();

      while(it.hasNext()) { 
         Map.Entry me = (Map.Entry)it.next();
         int Ci = ((Integer)me.getKey()).intValue();
         int value = ((Integer)me.getValue()).intValue();
         WF[Ci-1] = value;
      }

      return WF;
   }

   public void mouseClicked(MouseEvent e) {
      if (e.getButton() == MouseEvent.BUTTON1) {
         //DBG System.out.println("Button 1 clicked at " + e.getPoint().x + "," + e.getPoint().y);
         if (showEnergy == false) {
            for (int i = 0; i < config_pos.size(); i++) {
               if (config_pos.get(i).contains(e.getPoint().x, e.getPoint().y)) {
                  RightConfig = (int)config_val.get(i);

                  iHighlighted = i;

                  updateMatrixElement();

                  break;
               }
            }
         }
      }
      else if (e.getButton() == MouseEvent.BUTTON3) {
      }
   }

   public void mousePressed(MouseEvent e) {
   }

   public void mouseReleased(MouseEvent e) {
   }

   public void mouseEntered(MouseEvent e) {
      showBold = true;
      if (showEnergy == false) repaint();
   }

   public void mouseExited(MouseEvent e) {
      showBold = false;
      if (showEnergy == false) repaint();
   }

   private Rectangle drawEnergyScale(Graphics g, int width, int height, 
                                     double Energy, int Steps, boolean showResults) {
      int length = Math.min(width, height) / 24;
      int shift = Math.min(width, height) / 9;
      int dx = 5; // width / 3;

      int half_width = width / 2;

      double UpperLimit = CI.getUpperLimit();
      double LowerLimit = Math.round(exact_E * 10.0) / 10.0; // CI.getLowerLimit();

      String s1 = "" + LowerLimit;
      String s2 = "" + UpperLimit;

      Font font = g.getFont();
      FontMetrics metrics = g.getFontMetrics(font);

      int x = Math.max(metrics.stringWidth(s1), metrics.stringWidth(s2)) + dx + 3;
      int y1 = height - shift / 2;
      int y2 = shift / 2;

      g.drawLine(x, y1, x, y2);
      g.drawLine(x, y2, x - length / 3, y2 + length / 2);
      g.drawLine(x, y2, x + length / 3, y2 + length / 2);

      if (showResults) {
         Energy = Math.round(Energy * 1000.0) / 1000.0;

         String E = "You: E= " + Energy + ", Steps: " + Steps;

         g.drawString(E, x + 2 * length / 2, y2);

         int y3 = (int)((y1 - 2*y2) * (exact_E - UpperLimit) / (LowerLimit - UpperLimit));

         Color oldColor = g.getColor();

         g.setColor(Color.red);

         String Answer = "(" + Math.round(exact_E * 1000.0) / 1000.0 + ")";

         g.drawString(Answer, width - metrics.stringWidth(Answer) - 3, y2);

         g.drawLine(width - length, y3 + 2*y2, width, y3 + 2*y2);

         g.setColor(oldColor);
      }

      g.drawString(s1, x - metrics.stringWidth(s1) - dx, y1);

      g.drawString(s2, x - metrics.stringWidth(s2) - dx, 2 * y2);

      Rectangle rect = new Rectangle(x, 2*y2, length, y1 - 2*y2);

      return rect;
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
      int shift = height / 16;
      int size = result_WF.size();
      int d = width / (size + 1);

      double[] energy = new double [size];
      double[] intensity = new double [size];
      String[] configs = new String [size];

      Set set = result_WF.entrySet();
      Iterator it = set.iterator();

      if (config_pos.size() != 0) config_pos.clear();
      if (config_val.size() != 0) config_val.clear();

      double norm = 0.0;
      int counter = 0;
      double minIntencity = 0.0;

      while(it.hasNext()) { 
         Map.Entry me = (Map.Entry)it.next();
         int Ci = ((Integer)me.getKey()).intValue();
         configs[counter] = "" + Ci;
         double value = ((Integer)me.getValue()).doubleValue();
         norm += value * value;
         intensity[counter] = value;
         if (value < minIntencity) minIntencity = value;

         Rectangle rect = new Rectangle(d * (counter + 1) - 6, 0, 12, height);
         config_pos.add(rect);
         config_val.add(Ci);

         counter++;
      }

      if (counter != size) {
         System.err.println("Error in Score.paint: counter != size");
         System.exit(0);
      }

      double sqrtNorm = (size > 0) ? Math.sqrt(norm) : 1.0;

      int base = (int)((height - shift) * 1.0 / (1.0 + Math.abs(minIntencity) / sqrtNorm)) + shift;

      g.setColor(Color.green);

      Font font = g.getFont();
      FontMetrics metrics = g.getFontMetrics(font);

      shift = 2 * metrics.getHeight();

      if (setOfEnergies.isEmpty()) {
         if (isGameStarted) {
            if (showEnergy) drawEnergyScale(g, width, height, 0.0, 0, false);
         }
         else {
            String text[] = {"Try to get as close",
                             "as possible to the correct",
                             "Ground State Energy (" + (Math.round(exact_E * 1000.0) / 1000.0) + ")",
                             "by selecting as many",
                             "configurations as needed."};

            int h = height / (text.length + 1);

            int x, y;
            for (int i = 0; i < text.length; i++) {
               x = (width - metrics.stringWidth(text[i])) / 2;
               y = (i + 1) * h + metrics.getHeight() / 2 - 1;
               g.drawString (text[i], x, y);
            }
         }
      }
      else if (isGameOver) {
         String message = "Game over";

         int h = height / 2;

         int x = (width - metrics.stringWidth(message)) / 2;
         int y = h + metrics.getHeight() / 2 - 1;
         g.drawString (message, x, y);
      }

      if (showEnergy) {
         if (setOfEnergies.isEmpty() == false) {
            Rectangle rect = drawEnergyScale(g, width, height, setOfEnergies.lastElement(), 
                                             setOfEnergies.size(), true);

            int offset = 0;

            if ((rect.x + (setOfEnergies.size() - 1) * rect.width) > (width - 2 * rect.width)) {
               offset = rect.x + (setOfEnergies.size() - 1) * rect.width - (width - 2 * rect.width);
            }

            for (int i = 0; i < setOfEnergies.size(); i++) {
               int x = rect.x + i * rect.width - offset;

               if (x > rect.x / 2) {
                  double Energy = setOfEnergies.get(i);

                  double UpperLimit = CI.getUpperLimit();
                  double LowerLimit = Math.round(exact_E * 1000.0) / 1000.0; // CI.getLowerLimit();

                  int y3 = (int)(rect.height * (Energy - UpperLimit) / (LowerLimit - UpperLimit));

                  //DBG System.out.println("" + x);

                  g.drawLine(x, y3 + rect.y, x + rect.width, y3 + rect.y);
               }
            }
         }
      }
      else {
         for (int i = 0; i < size; i++) {
            int w = metrics.stringWidth(configs[i]);

            int y = (int)((base - shift) * intensity[i] / sqrtNorm);

            if (intensity[i] > 0.0) {
               g.drawLine(d * (i + 1), base, d * (i + 1), base - y);

               g.drawString(configs[i], d * (i + 1) - w / 2, base - y - 3);
            }
            else {
               g.drawLine(d * (i + 1), base - y, d * (i + 1), base);

               g.drawString(configs[i], d * (i + 1) - w / 2, base - 3);
            }

            if (i == iHighlighted) {
               if (intensity[i] > 0.0)
                  g.drawRect(d * (i + 1) - 1, base - y, 2, y);
               else
                  g.drawRect(d * (i + 1) - 1, base, 2, -y);

               String weight = "C of |" + configs[i] + "> = " + ((int)intensity[i]) + " / ";
               weight = weight + Math.round(sqrtNorm * 1000.0) / 1000.0 + " = ";
               weight = weight + Math.round((intensity[i] / sqrtNorm) * 1000.0) / 1000.0;

               g.drawString(weight, shift / 2, shift / 2);
            }
         }

         if (minIntencity < 0 && size > 0) {
            g.drawLine(0, base, width, base);
         }

         if (iHighlighted == -1 && size > 0) {
            Font oldFont = g.getFont();
            if (showBold) g.setFont(new Font(oldFont.getName(), Font.BOLD, oldFont.getSize()));

            g.drawString("Click on the peak for more info", shift / 2, shift / 2);

            if (showBold) g.setFont(oldFont);
         }
      }
   }
}
