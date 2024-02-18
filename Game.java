import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class Game extends Frame implements Runnable, ActionListener, ItemListener, WindowListener {

   int width, height;
   int iStep = 0;
   int allSteps = 20;
   int iConfigCount = 0;
   static Thread t = null;
   boolean threadSuspended;

   AmolComplex atom;
   Configurations configs;
   Orbitals orbitals;
   Score yourScore;
   Configurations config_in_wf;

   Hashtable<Integer,Integer> results = new Hashtable<Integer,Integer>();
   Hashtable<Integer,Double> result_E = new Hashtable<Integer,Double>();

   private Button config_down = new Button("<");
   private Button config_up = new Button(">");
   private Button enter_config = new Button("Enter");
   private Button subtract_config = new Button("Subtract");
   private TextField configs_value = new TextField("1", 5);
   private Label all_configs;

   private Button orbital_prev = new Button("<");
   private Button orbital_next = new Button(">");
   private TextField orbitals_value = new TextField("1", 3);
   private Label all_orbitals;

   private Button atom_start = new Button("Start");
   private Button atom_stop = new Button("Stop");

   private CheckboxGroup score_group = new CheckboxGroup();
   private Checkbox choose_E;
   private Checkbox choose_WF;

   private TextField matrix_element = new TextField("matrix elements", 20);

   private Checkbox help = new Checkbox("help", false);

   private Choice choose_model = new Choice();

   int theModel = 2;


   public static void main(String[] args) {
       Game myWindow = new Game("Adsorption Game", 800, 500);
       t = new Thread(myWindow);
       t.start();
   }

   public void windowClosing(WindowEvent e) {
       dispose();
       System.exit(0);
   }

   public void windowOpened(WindowEvent e) {}
   public void windowActivated(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowDeactivated(WindowEvent e) {}
   public void windowClosed(WindowEvent e) {}

   public Game(String title, int w, int h) {
      super(title);
      setSize(w, h);
      setVisible(true);

      addWindowListener(this);

      int L = 7;
      int Na = 4;
      int Nb = 3;

      theModel = 2;

      if (theModel == 1) {
         L = 7;
         Na = 4;
         Nb = 3;
      }
      else if (theModel == 2) {
         L = 11;
         Na = 6;
         Nb = 5;
      }

      int graph[][] = getStructure(theModel);

      double eps[] = orbitalEnergies(theModel, -0.1325, -0.1235);

      double integrals[] = calculateIntegrals(theModel, graph);


      setLayout(new GridLayout(2,1));

      atom = new AmolComplex(allSteps, L - 1);
      atom.setBackground(new Color(0,0,0));

      configs = new Configurations(L, Na, Nb, this, eps, graph);
      configs.setBackground(new Color(255,255,255));
      int i = configs.setConfig(1);
      configs_value.setText("" + i);
      configs.setHighlighted(1);
      int n = configs.getConfigsNumber();

      all_configs = new Label("/ " + n);

      orbitals = new Orbitals(1, L, eps);
      orbitals.setBackground(new Color(255,255,255));
      int k = orbitals.getOrbitalsNumber();

      all_orbitals = new Label("/ " + k);

      config_in_wf = new Configurations(L, Na, Nb, this, eps, graph);
      config_in_wf.setBackground(new Color(255,255,255));

      yourScore = new Score(config_in_wf, matrix_element, integrals);
      yourScore.setBackground(new Color(0,0,0));

      width = getSize().width;
      height = getSize().height;

      Insets insets = this.getInsets();
      int titleBarHeight = insets.top;

      configs.setSize(width / 2, height / 2 - 40 - titleBarHeight);
      atom.setSize(width / 2, height / 2 - 40 - titleBarHeight);
      orbitals.setSize(width / 3 - 1, height / 2 - 40 - titleBarHeight);
      yourScore.setSize(width / 3 - 1, height / 2 - 40 - titleBarHeight);
      config_in_wf.setSize(width / 3 - 1, height / 2 - 40 - titleBarHeight);

      setBackground(Color.lightGray);

      config_down.addActionListener(this);
      config_up.addActionListener(this);
      enter_config.addActionListener(this);
      subtract_config.addActionListener(this);
      configs_value.addActionListener(this);

      orbital_prev.addActionListener(this);
      orbital_next.addActionListener(this);
      orbitals_value.addActionListener(this);

      atom_start.addActionListener(this);
      atom_stop.addActionListener(this);

      Panel upper_panel = new Panel();
      upper_panel.setLayout(new GridLayout(1,2));

      Panel lower_panel = new Panel();
      lower_panel.setLayout(new GridLayout(1,2));

      enter_config.setEnabled(false);
      subtract_config.setEnabled(false);

      Panel p1 = new Panel();
      p1.add(configs);
      p1.add(config_down);
      p1.add(config_up);
      p1.add(configs_value);
      p1.add(all_configs);
      p1.add(enter_config);
      p1.add(subtract_config);

      upper_panel.add(p1);

      Panel p2 = new Panel();
      p2.add(atom);
      p2.add(atom_start);
      p2.add(atom_stop);

      help.addItemListener(this);

      p2.add(help);

      choose_model.addItem("Model 1");
      choose_model.addItem("Model 2");
      choose_model.select(theModel - 1);

      choose_model.addItemListener(this);

      p2.add(choose_model);

      upper_panel.add(p2);

      add(upper_panel);

      Panel p3 = new Panel();
      p3.add(yourScore);

      choose_E = new Checkbox("Energy", score_group, true);
      choose_E.addItemListener(this);

      choose_WF = new Checkbox("Wave Function", score_group, false);
      choose_WF.addItemListener(this);

      p3.add(choose_E);
      p3.add(choose_WF);

      lower_panel.add(p3);

      Panel p4 = new Panel();
      p4.add(config_in_wf);
      matrix_element.setEnabled(false);
      p4.add(matrix_element);

      lower_panel.add(p4);

      Panel p5 = new Panel();
      p5.add(orbitals);
      p5.add(orbital_prev);
      p5.add(orbital_next);
      p5.add(orbitals_value);
      p5.add(all_orbitals);

      lower_panel.add(p5);

      add(lower_panel);

      validate();
   }

   public static int[][] getStructure(int param) {
      if (param == 1) {
         int graph[][] = { { 0, 1 } };

         return graph;
      }
      else if (param == 2) {
         int graph[][] = { { 0, 1 },
                           { 0, 4 },
                           { 0, 8 } };

         return graph;
      }
      else {
         return null;
      }
   }

   public static double[] orbitalEnergies(int param, double alpha, double beta) {
      int L = 7;

      if (param == 1)
         L = 7;
      else if (param == 2)
         L = 11;

      double eps[] = new double[L];

      if (param == 1) {
         eps[0] = -0.5;
         eps[1] = alpha + 2 * beta;
         eps[2] = alpha + beta;
         eps[3] = alpha + beta;
         eps[4] = alpha - beta;
         eps[5] = alpha - beta;
         eps[6] = alpha - 2 * beta;
      }
      else if (param == 2) {
         eps[0]  = -0.5;
         eps[1]  = alpha + 2.303 * beta;
         eps[2]  = alpha + 1.618 *  beta;
         eps[3]  = alpha + 1.303 * beta;
         eps[4]  = alpha + beta;
         eps[5]  = alpha + 0.618 * beta;
         eps[6]  = alpha - 0.618 * beta;
         eps[7]  = alpha - beta;
         eps[8]  = alpha - 1.303 * beta;
         eps[9]  = alpha - 1.618 * beta;
         eps[10] = alpha - 2.303 * beta;
      }

      for (int i = 1; i < eps.length; i++) eps[i] = Math.round(eps[i] * 1000.0) / 1000.0;

      return eps;
   }

   public static double[] calculateIntegrals(int param, int[][] graph) {
      int N = 1;

      if (param == 1)
         N = 1;
      else if (param == 2)
         N = 3;

      if (N != graph.length) {
         System.err.println("Wrong size in calculateIntegrals");
         System.exit(0);
      }

      double integrals[] = new double[N];

      if (param == 1) {
         integrals[0] = 0.504;
      }
      else if (param == 2) {
         double x = -0.8; // -1.0;

         integrals[0] = x * (-0.46 - 0.46);
         integrals[1] = x * (0.41 + 0.41);
         integrals[2] = x * (0.35 + 0.35);
      }

      return integrals;
   }

   public void destroy() {
   }

   public void start() {
      if ( t == null ) {
         t = new Thread( this );
         threadSuspended = false;
         t.start();
      }
      else {
         if ( threadSuspended ) {
            threadSuspended = false;
            synchronized( this ) {
               notify();
            }
         }
      }
   }

   public void stop() {
      threadSuspended = true;
   }

   public void run() {
      try {
         while (true) {

            if (atom.getStarted()) {
               ++iStep;
               if (iStep == allSteps) {
                  updateConfig(1);
                  iStep = 0;
                  yourScore.repaint();
               }
               setTitle("Adsorption Game: Step " + (iStep + 1) + " out of " + allSteps);
               atom.setCounter(iStep);
            }
            else {
               setTitle("Adsorption Game: Press Start to begin the game");
            }

            if ( threadSuspended ) {
               synchronized( this ) {
                  while ( threadSuspended ) {
                     wait();
                  }
               }
            }
            repaint();
            atom.repaint();
            t.sleep(1000);  // interval given in milliseconds
         }
      }
      catch (InterruptedException e) { }
   }

   public void paint(Graphics g) {
      if (((width != getSize().width) || (height != getSize().height)) && this.configs != null) {

         width = getSize().width;
         height = getSize().height;

         Insets insets = this.getInsets();
         int titleBarHeight = insets.top;

         configs.setSize(width / 2, height / 2 - 40 - titleBarHeight);
         atom.setSize(width / 2, height / 2 - 40 - titleBarHeight);
         orbitals.setSize(width / 3 - 1, height / 2 - 40 - titleBarHeight);
         yourScore.setSize(width / 3 - 1, height / 2 - 40 - titleBarHeight);
         config_in_wf.setSize(width / 3 - 1, height / 2 - 40 - titleBarHeight);

         validate();
      }
   }

   private void updateConfig(int value) {
         int one = (value > 0) ? 1 : -1;

         int i = configs.getConfig();

         String str = configs_value.getText();
         int iConfig = Integer.parseInt(str);

         if (i != iConfig) {
            i = configs.setConfig(iConfig);
            configs_value.setText("" + i);
            configs.repaint();
         }

         iConfigCount++;

         if (results.containsKey(i)) {
            int weight = ((Integer)results.get(i)).intValue();
            results.put(Integer.valueOf(i), Integer.valueOf(weight + one));
         }
         else {
            results.put(Integer.valueOf(i), Integer.valueOf(one));

            double Energy = configs.getEnergyOfConfig(i);
            result_E.put(Integer.valueOf(i), Double.valueOf(Energy));
         }

         boolean win = yourScore.copyHashtables(results, result_E, i);

         if (win) {
            atom.setStopped(iConfigCount);
            atom.setWinner(iConfigCount);
            enter_config.setEnabled(false);
            subtract_config.setEnabled(false);
            yourScore.setGameOver(win);
            choose_model.setEnabled(true);
            atom.repaint();
            yourScore.repaint();
         }
   }

   public void itemStateChanged(ItemEvent ie) {
      if (ie.getItemSelectable() == choose_E) {
         yourScore.setChoice(true);
         yourScore.repaint();
      }
      else if (ie.getItemSelectable() == choose_WF) {
         yourScore.setChoice(false);
         yourScore.repaint();
      }
      else if (ie.getItemSelectable() == help) {
         String message = (ie.getStateChange() == ItemEvent.SELECTED) ? "selected." : "unselected.";
         System.out.println("Help mode is " + message);
         boolean mode = (ie.getStateChange() == ItemEvent.SELECTED) ? true : false;
         configs.setHelpMode(mode);
         orbitals.setHelpMode(mode);
         configs.repaint();
         orbitals.repaint();
      }
      else if (ie.getItemSelectable() == choose_model) {
         String selection = choose_model.getSelectedItem();
         System.out.println(selection + " is selected");

         if (selection == "Model 1" && theModel == 2) {
            theModel = 1;

            int L = 7;
            int Na = 4;
            int Nb = 3;

            int graph[][] = getStructure(theModel);

            double eps[] = orbitalEnergies(theModel, -0.1325, -0.1235);

            double integrals[] = calculateIntegrals(theModel, graph);

            atom.changeModel(L - 1);

            configs.changeModel(L, Na, Nb, eps, graph);
            int i = configs.setConfig(1);
            configs_value.setText("" + i);
            configs.setHighlighted(1);
            configs.repaint();

            int n = configs.getConfigsNumber();

            all_configs.setText("/ " + n);

            orbitals.changeModel(1, L, eps);
            orbitals.repaint();

            orbitals_value.setText("" + 1);

            int k = orbitals.getOrbitalsNumber();
            all_orbitals.setText("/ " + k);

            config_in_wf.changeModel(L, Na, Nb, eps, graph);
            config_in_wf.disableConfig();
            config_in_wf.repaint();

            yourScore.changeModel(integrals);
            yourScore.repaint();

            matrix_element.setText("matrix elements");
         }
         else if (selection == "Model 2" && theModel == 1) {
            theModel = 2;

            int L = 11;
            int Na = 6;
            int Nb = 5;

            int graph[][] = getStructure(theModel);

            double eps[] = orbitalEnergies(theModel, -0.1325, -0.1235);

            double integrals[] = calculateIntegrals(theModel, graph);

            atom.changeModel(L - 1);

            configs.changeModel(L, Na, Nb, eps, graph);
            int i = configs.setConfig(1);
            configs_value.setText("" + i);
            configs.setHighlighted(1);
            configs.repaint();

            int n = configs.getConfigsNumber();

            all_configs.setText("/ " + n);

            orbitals.changeModel(1, L, eps);
            orbitals.repaint();

            orbitals_value.setText("" + 1);

            int k = orbitals.getOrbitalsNumber();
            all_orbitals.setText("/ " + k);

            config_in_wf.changeModel(L, Na, Nb, eps, graph);
            config_in_wf.disableConfig();
            config_in_wf.repaint();

            yourScore.changeModel(integrals);
            yourScore.repaint();

            matrix_element.setText("matrix elements");
         }
      }
   }

   public void actionPerformed(ActionEvent e) {
      if (e.getSource() == config_down) {
         int i = configs.Decrease();
         configs_value.setText("" + i);
         configs.repaint();

         yourScore.setLeftConfig(i);
         yourScore.updateMatrixElement();
      }
      else if (e.getSource() == config_up) {
         int i = configs.Increase();
         configs_value.setText("" + i);
         configs.repaint();

         yourScore.setLeftConfig(i);
         yourScore.updateMatrixElement();
      }
      else if (e.getSource() == enter_config) {
         updateConfig(1);

         iStep = 0;

         setTitle("Adsorption Game: Step " + (iStep + 1) + " out of " + allSteps);
         atom.setCounter(iStep);
         repaint();
         atom.repaint();

         yourScore.repaint();
      }
      else if (e.getSource() == subtract_config) {
         updateConfig(-1);

         iStep = 0;

         setTitle("Adsorptiion Game: Step " + (iStep + 1) + " out of " + allSteps);
         atom.setCounter(iStep);
         repaint();
         atom.repaint();

         yourScore.repaint();
      }
      else if (e.getSource() == configs_value) {
         String str = configs_value.getText();
         int iConfig = Integer.parseInt(str);

         iConfig = configs.setConfig(iConfig);
         configs_value.setText("" + iConfig);
         yourScore.setLeftConfig(iConfig);
         repaint();
         configs.repaint();

         yourScore.setLeftConfig(iConfig);
         yourScore.updateMatrixElement();
      }
      else if (e.getSource() == orbital_prev) {
         int i = orbitals.Previous();
         orbitals_value.setText("" + i);
         configs.setHighlighted(i);
         orbitals.repaint();
         configs.repaint();
      }
      else if (e.getSource() == orbital_next) {
         int i = orbitals.Next();
         orbitals_value.setText("" + i);
         configs.setHighlighted(i);
         orbitals.repaint();
         configs.repaint();
      }
      else if (e.getSource() == orbitals_value) {
         String str = orbitals_value.getText();
         int iOrbital = Integer.parseInt(str);

         iOrbital = orbitals.setOrbital(iOrbital);
         orbitals_value.setText("" + iOrbital);
         configs.setHighlighted(iOrbital);
         orbitals.repaint();
         configs.repaint();
      }
      else if (e.getSource() == atom_start) {
         atom.setStarted();
         enter_config.setEnabled(true);
         if (theModel == 2) subtract_config.setEnabled(true);
         choose_model.setEnabled(false);
         iStep = 0;

         results.clear();
         yourScore.clearResults();
         config_in_wf.disableConfig();
         iConfigCount = 0;

         atom.repaint();
         yourScore.repaint();
         config_in_wf.repaint();
         matrix_element.setText("matrix elements");
      }
      else if (e.getSource() == atom_stop) {
         atom.setStopped(iConfigCount);
         enter_config.setEnabled(false);
         subtract_config.setEnabled(false);
         choose_model.setEnabled(true);
         atom.repaint();
      }
   }

   public void notifyEvent(String str, int iOrbital) {
      iOrbital = orbitals.setOrbital(iOrbital);
      orbitals_value.setText("" + iOrbital);
      configs.setHighlighted(iOrbital);
      orbitals.repaint();
      configs.repaint();
   }
}

