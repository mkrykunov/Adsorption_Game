import java.math.*;

public class LinearAlgebra {

   public static void tred2(double[][] a, int n, double d[], double e[])
   {
	int l,k,j,i;
	double scale,hh,h,g,f;

	for (i = n - 1; i >= 1; i--) {
		l = i - 1;
		h=scale=0.0;
		if (l > 0) {
			for (k = 0; k <= l; k++)
				scale += Math.abs(a[i][k]);
			if (scale == 0.0)
				e[i]=a[i][l];
			else {
				for (k = 0; k <= l; k++) {
					a[i][k] /= scale;
					h += a[i][k]*a[i][k];
				}
				f=a[i][l];
				g = f>0 ? -Math.sqrt(h) : Math.sqrt(h);
				e[i]=scale*g;
				h -= f*g;
				a[i][l]=f-g;
				f=0.0;
				for (j = 0; j <= l; j++) {
				/* Next statement can be omitted if eigenvectors not wanted */
					a[j][i]=a[i][j]/h;
					g=0.0;
					for (k = 0; k <= j; k++)
						g += a[j][k]*a[i][k];
					for (k = j + 1; k <= l; k++)
						g += a[k][j]*a[i][k];
					e[j]=g/h;
					f += e[j]*a[i][j];
				}
				hh=f/(h+h);
				for (j = 0; j <= l; j++) {
					f=a[i][j];
					e[j]=g=e[j]-hh*f;
					for (k = 0; k <= j; k++)
						a[j][k] -= (f*e[k]+g*a[i][k]);
				}
			}
		} else
			e[i]=a[i][l];
		d[i]=h;
	}
	/* Next statement can be omitted if eigenvectors not wanted */
	d[0]=0.0;
	e[0]=0.0;
	/* Contents of this loop can be omitted if eigenvectors not
			wanted except for statement d[i]=a[i][i]; */
	for (i = 0; i < n; i++) {
		l = i - 1;
		if (d[i] != 0.0) {
			for (j = 0; j <= l; j++) {
				g=0.0;
				for (k = 0; k <= l; k++)
					g += a[i][k]*a[k][j];
				for (k = 0; k <= l; k++)
					a[k][j] -= g*a[k][i];
			}
		}
		d[i]=a[i][i];
		a[i][i]=1.0;
		for (j = 0; j <= l; j++) a[j][i]=a[i][j]=0.0;
	}
   }

   public static double SIGN(double a, double b) {
      return ((b)<0 ? -Math.abs(a) : Math.abs(a));
   }

   public static double SQR(double a) {
      return ((a)*(a));
   }

   public static void nrerror2(String error_text)
   /* Numerical Recipes standard error handler */
   {
	System.out.println("Numerical Recipes run-time error...");
	System.out.println(error_text);
	System.out.println("...now exiting to system...\n");
	System.exit(0);
   }

   public static double pythag(double a, double b)
   {
	double absa,absb;
	
	absa=Math.abs(a);
	absb=Math.abs(b);
	
	if (absa > absb) 
		return absa*Math.sqrt(1.0+SQR(absb/absa));
	else 
		return (absb == 0.0 ? 0.0 : absb*Math.sqrt(1.0+SQR(absa/absb)));
   }

   public static void tqli(double d[], double e[], int n, double[][] z)
   {
	int m,l,iter,i,k;
	double s,r,p,g,f,dd,c,b;

	for (i = 1; i < n; i++) e[i-1]=e[i];
	e[n-1]=0.0;
	for (l = 0; l < n; l++) {
		iter=0;
		do {
			for (m = l; m < n - 1; m++) {
				dd=Math.abs(d[m])+Math.abs(d[m+1]);
				if (Math.abs(e[m])+dd == dd) break;
			}
			if (m != l) {
				if (iter++ == 30) nrerror2("Too many iterations in TQLI");
				g=(d[l+1]-d[l])/(2.0*e[l]);
				r=pythag(g,1.0);
				g=d[m]-d[l]+e[l]/(g+SIGN(r,g));
				s=c=1.0;
				p=0.0;
				for (i = m - 1; i >= l; i--) {
					f=s*e[i];
					b=c*e[i];
					e[i+1]=(r=pythag(f,g));
					if (r == 0.0) {
						d[i+1] -= p;
						e[m] = 0.0;
						break;
					}
					s=f/r;
					c=g/r;
					g=d[i+1]-p;
					r=(d[i]-g)*s+2.0*c*b;
					p=s*r;
					d[i+1]=g+p;
					g=c*r-b;
					/* Next loop can be omitted if eigenvectors not wanted */
					for (k = 0; k < n; k++) {
						f=z[k][i+1];
						z[k][i+1]=s*z[k][i]+c*f;
						z[k][i]=c*z[k][i]-s*f;
					}
				}
				if (r == 0.0 && i >= l) continue;
				d[l] -= p;
				e[l]=g;
				e[m]=0.0;
			}
		} while (m != l);
	}
   }

   public static void sort(int n, int SMALL, double d[], double[][] v)
   {
        int i, j, k;
        double p;

        for (i = 0; i < n-1; i++)
        {
           k = i;
           p = d[i];
           for (j = i+1; j < n; j++)
           {
              if (SMALL != 0)
              {
                 if (d[j] <= p)
                 {
                    k = j;
                    p = d[j];
                 }
              }
              else
              {
                 if (d[j] >= p)
                 {
                    k = j;
                    p = d[j];
                 }
              }
           }

           if (k != i)
           {
              d[k] = d[i];
              d[i] = p;
              for (j = 0; j < n; j++)
              {
                 p = v[j][i];
                 v[j][i] = v[j][k];
                 v[j][k] = p;
              }
           }
        }
   }
}
