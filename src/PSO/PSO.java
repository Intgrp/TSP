package PSO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;


public class PSO {
	  
	    private int bestNum;  
	    private float w;  
	    private int MAX_GEN;// the times of evolution  
	    private int scale;// the scale of population  
	  
	    private int cityNum; // the number of city  
	    private int t;// current generation  
	  
	    private int[][] distance; // distance matrix  
	      
	    private int[][] oPopulation;// particle swarm
	    private ArrayList<ArrayList<SO>> listV;// the initial exchange order of every particle  
	  
	    private int[][] Pd;// the best solution of a particle in the past，  
	    private int[] vPd;// the value of the solution  
	  
	    private int[] Pgd;// the best solution of the whole particle swarm in the past
	    private int vPgd;// the value of the best solution
	    private int bestT;// the best generation  
	  
	    private int[] fitness;// the fitness of population  
	  
	    private Random random;  
	  
	    public PSO() {  
	  
	    }  
	  
	    /** 
	     * constructor of GA 
	     *  
	     * @param n  the number of city 
	     *            
	     * @param g  evolutionary generation
	     * 
	     *@param  s   the scale of population  
	     *            
	     * @param w  weight
	     *            
	     **/  
	    public PSO(int n, int g, int s, float w) {  
	        this.cityNum = n;  
	        this.MAX_GEN = g;  
	        this.scale = s;  
	        this.w = w;  
	    }  
	  
	    // 给编译器一条指令，告诉它对被批注的代码元素内部的某些警告保持静默  
	    @SuppressWarnings("resource")  
	    /** 
	     * 初始化PSO算法类 
	     * @param filename 数据文件名，该文件存储所有城市节点坐标数据 
	     * @throws IOException 
	     */  
	    private void init(String filename) throws IOException {  
	        // read the data
	        int[] x;  
	        int[] y;  
	        String strbuff;  
	        BufferedReader data = new BufferedReader(new InputStreamReader(  
	                new FileInputStream(filename)));  
	        distance = new int[cityNum][cityNum];  
	        x = new int[cityNum];  
	        y = new int[cityNum];  
	        while ((strbuff = data.readLine())!=null) {
				if (!Character.isAlphabetic(strbuff.charAt(0)))
					break;
			}
			String[] tmp = strbuff.split(" ");
			x[0] = Integer.valueOf(tmp[1]);// x坐标
			y[0] = Integer.valueOf(tmp[2]);// y坐标
	        for (int i = 1; i < cityNum; i++) {  
	            // read data one row，data format 1 6734 1453  
	            strbuff = data.readLine();  
	            // character segmentation   
	            String[] strcol = strbuff.split(" ");  
	            x[i] = Integer.valueOf(strcol[1]);// x coordinates  
	            y[i] = Integer.valueOf(strcol[2]);// y coordinates
	        }  
	        // calculate the distance matrix
	        // ，针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628  
	        for (int i = 0; i < cityNum - 1; i++) {  
	            distance[i][i] = 0; // the diagonal line is 0   
	            for (int j = i + 1; j < cityNum; j++) {  
	                double rij = Math  
	                        .sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j])  
	                                * (y[i] - y[j])) / 10.0);  
	                // rounding  
	                int tij = (int) Math.round(rij);  
	                if (tij < rij) {  
	                    distance[i][j] = tij + 1;  
	                    distance[j][i] = distance[i][j];  
	                } else {  
	                    distance[i][j] = tij;  
	                    distance[j][i] = distance[i][j];  
	                }  
	            }  
	        }  
	        distance[cityNum - 1][cityNum - 1] = 0;  
	  
	        oPopulation = new int[scale][cityNum];  
	        fitness = new int[scale];  
	  
	        Pd = new int[scale][cityNum];  
	        vPd = new int[scale];  
	  
	        
	  
	        Pgd = new int[cityNum];  
	        vPgd = Integer.MAX_VALUE;  
	  
	       
	  
	        bestT = 0;  
	        t = 0;  
	  
	        random = new Random(System.currentTimeMillis());  
	        
	  
	    }  
	  
	    // initial population 
	    void initGroup() {  
	        int i, j, k;  
	        for (k = 0; k < scale; k++)// the number of population
	        {  
	            oPopulation[k][0] = random.nextInt(65535) % cityNum;  
	            for (i = 1; i < cityNum;)// the number of particle
	            {  
	                oPopulation[k][i] = random.nextInt(65535) % cityNum;  
	                for (j = 0; j < i; j++) {  
	                    if (oPopulation[k][i] == oPopulation[k][j]) {  
	                        break;  
	                    }  
	                }  
	                if (j == i) {  
	                    i++;  
	                }  
	            }  
	        }  
	  
	      
	    }  
	  
	    void initListV() {  
	        int ra;  
	        int raA;  
	        int raB;  
	  
	        listV = new ArrayList<ArrayList<SO>>();  
	  
	        for (int i = 0; i < scale; i++) {  
	            ArrayList<SO> list = new ArrayList<SO>();  
	            ra = random.nextInt(65535) % cityNum;  
	            for (int j = 0; j < ra; j++) {  
	                raA = random.nextInt(65535) % cityNum;  
	                raB = random.nextInt(65535) % cityNum;  
	                while (raA == raB) {  
	                    raB = random.nextInt(65535) % cityNum;  
	                }  
	  
	                // raA与raB不一样  
	                SO s = new SO(raA, raB);  
	                list.add(s);  
	            }  
	  
	            listV.add(list);  
	        }  
	    }  
	  
	    public int evaluate(int[] chr) {  
	        // 0123  
	        int len = 0;  
	        // 编码，起始城市,城市1,城市2...城市n  
	        for (int i = 1; i < cityNum; i++) {  
	            len += distance[chr[i - 1]][chr[i]];  
	        }  
	        // 城市n,起始城市  
	        len += distance[chr[cityNum - 1]][chr[0]];  
	        return len;  
	    }  
	  
	    // 求一个基本交换序列作用于编码arr后的编码  
	    public void add(int[] arr, ArrayList<SO> list) {  
	        int temp = -1;  
	        SO s;  
	        for (int i = 0; i < list.size(); i++) {  
	            s = list.get(i);  
	            temp = arr[s.getX()];  
	            arr[s.getX()] = arr[s.getY()];  
	            arr[s.getY()] = temp;  
	        }  
	    }  
	  
	    // 求两个编码的基本交换序列，如A-B=SS  
	    public ArrayList<SO> minus(int[] a, int[] b) {  
	        int[] temp = b.clone();  
	        /* 
	         * int[] temp=new int[L]; for(int i=0;i<L;i++) { temp[i]=b[i]; } 
	         */  
	        int index;  
	        // Commutants 
	        SO s;  
	        // exchange order
	        ArrayList<SO> list = new ArrayList<SO>();  
	        for (int i = 0; i < cityNum; i++) {  
	            if (a[i] != temp[i]) {  
	                // 在temp中找出与a[i]相同数值的下标index  
	                index = findNum(temp, a[i]);  
	                // exchange the value between the index of i and index in the temp
	                changeIndex(temp, i, index);  
	                // remember Commutants 
	                s = new SO(i, index);  
	                // save Commutants 
	                list.add(s);  
	            }  
	        }  
	        return list;  
	    }  
	  
	    // find the num in the array arr,return the index of num 
	    public int findNum(int[] arr, int num) {  
	        int index = -1;  
	        for (int i = 0; i < cityNum; i++) {  
	            if (arr[i] == num) {  
	                index = i;  
	                break;  
	            }  
	        }  
	        return index;  
	    }  
	  
	    // exchange the index between index1 and index2  in the array arr
	    public void changeIndex(int[] arr, int index1, int index2) {  
	        int temp = arr[index1];  
	        arr[index1] = arr[index2];  
	        arr[index2] = temp;  
	    }  
	  
	    // two-dimensional array copy
	    public void copyarray(int[][] from, int[][] to) {  
	        for (int i = 0; i < scale; i++) {  
	            for (int j = 0; j < cityNum; j++) {  
	                to[i][j] = from[i][j];  
	            }  
	        }  
	    }  
	  
	    // One-dimensional array copy
	    public void copyarrayNum(int[] from, int[] to) {  
	        for (int i = 0; i < cityNum; i++) {  
	            to[i] = from[i];  
	        }  
	    }  
	      
	    public void evolution() {  
	        int i, j, k;  
	        int len = 0;  
	        float ra = 0f;  
	  
	        ArrayList<SO> Vi;  
	          
	        // evolution once
	        for (t = 0; t < MAX_GEN; t++) {  
	            // for every particle
	            for (i = 0; i < scale; i++) {  
	                if(i==bestNum) continue;  
	                ArrayList<SO> Vii = new ArrayList<SO>();  
	                //System.out.println("------------------------------");  
	                // update the speed
	                // Vii=wVi+ra(Pid-Xid)+rb(Pgd-Xid)  
	                Vi = listV.get(i);  
	  
	                // wVi+表示获取Vi中size*w取整个交换序列  
	                len = (int) (Vi.size() * w);  
	                //越界判断  
	                //if(len>cityNum) len=cityNum;  
	                //System.out.println("w:"+w+" len:"+len+" Vi.size():"+Vi.size());  
	                for (j = 0; j < len; j++) {  
	                    Vii.add(Vi.get(j));  
	                }  
	  
	                // Pid-Xid  
	                ArrayList<SO> a = minus(Pd[i], oPopulation[i]);  
	                ra = random.nextFloat();  
	  
	                // ra(Pid-Xid)+  
	                len = (int) (a.size() * ra);  
	                //越界判断  

	                for (j = 0; j < len; j++) {  
	                    Vii.add(a.get(j));  
	                }  
	  
	                // Pid-Xid  
	                ArrayList<SO> b = minus(Pgd, oPopulation[i]);  
	                ra = random.nextFloat();  
	  
	                // ra(Pid-Xid)+  
	                len = (int) (b.size() * ra);  
	                //越界判断  
	                //if(len>cityNum) len=cityNum;  
	                //System.out.println("ra:"+ra+" len:"+len+" b.size():"+b.size());  
	                for (j = 0; j < len; j++) {  
	                    SO tt= b.get(j);  
	                    Vii.add(tt);  
	                }  

	                // preserve the new Vii  
	                listV.add(i, Vii);  
	  
	                // update the position
	                // Xid’=Xid+Vid  
	                add(oPopulation[i], Vii);  
	            }  
	  
	            // calculte the fitness[k] of new particle ,select the best solution
	            for (k = 0; k < scale; k++) {  
	                fitness[k] = evaluate(oPopulation[k]);  
	                if (vPd[k] > fitness[k]) {  
	                    vPd[k] = fitness[k];  
	                    copyarrayNum(oPopulation[k], Pd[k]);  
	                    bestNum=k;  
	                }  
	                if (vPgd > vPd[k]) {  
	                    System.out.println("bestLength"+vPgd+" generation："+bestT);  
	                    bestT = t;  
	                    vPgd = vPd[k];  
	                    copyarrayNum(Pd[k], Pgd);  
	                }  
	            }         
	        }  
	    }  
	  
	    public void solve() {  
	        int i;  
	        int k;  
	  
	        initGroup();  
	        initListV();  
	  
	        // every particle remember the own best solution
	        copyarray(oPopulation, Pd);  
	  
	        // calculate the fitness[k] of initial population,Fitness[max],select the best solution  
	        for (k = 0; k < scale; k++) {  
	            fitness[k] = evaluate(oPopulation[k]);  
	            vPd[k] = fitness[k];  
	            if (vPgd > vPd[k]) {  
	                vPgd = vPd[k];  
	                copyarrayNum(Pd[k], Pgd);  
	                bestNum=k;  
	            }  
	        }  
	  
	        
	        System.out.println("initial particle swarm...");  
	        for (k = 0; k < scale; k++) {  
	            for (i = 0; i < cityNum; i++) {  
	                System.out.print(oPopulation[k][i] + ",");  
	            }  
	            System.out.println();  
	            System.out.println("----" + fitness[k]);  
	  
	           
	        }  
	  
	        // evoluton
	        evolution();  
	  
	     
	        System.out.println("final particle swarm...");  
	        for (k = 0; k < scale; k++) {  
	            for (i = 0; i < cityNum; i++) {  
	                System.out.print(oPopulation[k][i] + ",");  
	            }  
	            System.out.println();  
	            System.out.println("----" + fitness[k]);  
	  
	           
	        }  
	          
	        System.out.print("bestT:");  
	        System.out.println(bestT);  
	        System.out.print("bestLength");  
	        System.out.println(vPgd);  
	        System.out.println("bestTour：");  
	        System.out.print(Pgd[0]);  
	        for (i = 0; i < cityNum; i++) {  
	            System.out.print("->"+Pgd[i]);  
	        }  
	  
	    }  
	      
	  
	    /** 
	     * @param args 
	     * @throws IOException 
	     */  
	    public static void main(String[] args) throws IOException {  
	        System.out.println("Start....");  
	  
	        PSO pso = new PSO(48, 1000, 20, 0.5f);  
	        pso.init("resources/att48.tsp");  
	        pso.solve();  
	    }  
}
