import java.io.*;

class Combinator {

    int m_nSize;
    int m_nBase;
    int m_nConfigs;
    int m_nCounter;
    int m_First;
    int m_Second;
    int m_Residue;
    int[] P;


    public Combinator (int a, int b) {
       P = new int [a];

       int k = (a - b > b) ? a - b : b;
       m_nSize    = a;
       m_nBase    = b;
       m_Residue  = a - k;
       m_nConfigs = (int)(Factorial(a, k) / Factorial(a - k, 1));
    }

    public double Factorial(int a, int b)
    {
        double f = 1.0;

        if (b > a)
        {
                int c = b;
                b = a;
                a = c;
        }

        if (b == 0 || b == a)   return  f;

        f = a;
        while (--a > b)   f *= a;

        return  f;
    }

    public int GetConfigs()
    {
       return m_nConfigs;
    }

    public void CreateConfigs(int Storage[][], int a, int b)
    {
        m_First = a;
        m_Second = b;

        if (m_nBase != m_Residue)
        {
           int save = m_First;
           m_First = m_Second;
           m_Second = save;
        }

        m_nCounter = -1;
        Combination(Storage, m_nSize, m_Residue);

        if (m_nCounter+1 != m_nConfigs)
        {
           System.err.println("Error in CreateConfigs: m_nCounter+1 != m_nConfigs");
           System.exit(0);
        }

        return;
    }

    private void Combination(int Array[][], int n, int k)
    {
        int p = --k;

        for (int i = 0; i <= k; i++)    P[i] = i;
        MakeBinaryVector(m_nSize, m_Residue, Array[++m_nCounter], P);

        while (p >= 0)
        {
                p = (P[k] == n-1) ? p-1 : k;

                if (p >= 0)
                {
                        for (int i = k; i >= p; i--)
                                P[i] = P[p] + i - p + 1;
                        MakeBinaryVector(m_nSize, m_Residue, Array[++m_nCounter], P);
                }
        }
    }

    private void MakeBinaryVector(int size, int begin, int dest[], int[] source)
    {
        for (int j = 0; j < size; j++)
                dest[j] = m_Second;
        for (int j = 0; j < begin; j++)
                dest[source[j]] = m_First;
    }

    public int PreviousTest(int Array[][])
    {
       for (int i = 1; i < m_nConfigs; i++)
       {
          for (int j = 0; j < i; j++)
          {
             if (VectorCompare(Array[i], Array[j]) == 0)
             {
                System.err.println("Equivalent configurations : " + i + "," + j);
                return 0;
             }
          }
       }

       return  1;
    }

    private int VectorCompare(int arg1[], int arg2[])
    {
       for (int k = 0; k < m_nSize; k++)
          if (arg1[k] != arg2[k]) return  -1;

       return  0;
    }
}
