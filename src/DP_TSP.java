import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class DP_TSP {
	public int cityNum; // 城市数量
	public long[][] distance; // 距离矩阵
 
	public int[] colable;//代表列，也表示是否走过，走过置0
	public int[] row;//代表行，选过置0
	
	public DP_TSP(int n) {
		cityNum = n;
	}
	
	public void init(String filename) throws IOException {
		// 读取数据
		long[] x;
		long[] y;
		String strbuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(
				new FileInputStream(filename)));
		distance = new long[cityNum][cityNum];
		x = new long[cityNum];
		y = new long[cityNum];
		//过滤头几行无用的说明
		while ((strbuff = data.readLine())!=null) {
			if (!Character.isAlphabetic(strbuff.charAt(0)))
				break;
		}
		String[] tmp = strbuff.split(" ");
		x[0] = Long.valueOf(tmp[1]);// x坐标
		y[0] = Long.valueOf(tmp[2]);// y坐标
		
		for (int i = 1; i < cityNum; i++) {
			// 读取一行数据，数据格式1 6734 1453
			strbuff = data.readLine();
			// 字符分割
			String[] strcol = strbuff.split(" ");
			x[i] = Long.valueOf(strcol[1]);// x坐标
			y[i] = Long.valueOf(strcol[2]);// y坐标
		}
		data.close();
 
		// 计算距离矩阵
		// ，针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
		for (int i = 0; i < cityNum; i++) {
			for (int j = 0; j < cityNum; j++) {
				distance[i][j] = EUC_2D_dist(x[i] , x[j] ,y[i] , y[j]);
			}
		}
	}
	
	public long EUC_2D_dist(long x1,long x2, long y1,long y2) {
		return (long) Math.sqrt(((x1 - x2) * (x1 - x2) + (y1 - y2)	* (y1 - y2)));
	}
	
	public long DP() {
		if (distance==null) {
			System.out.println("距离矩阵为空");
		}
		int cityCount = cityNum;
		long roadmap[][] = distance;        //转成邻接矩阵方便取数
		int[][] pos = new int[cityCount][1 << (cityCount - 1)];  //记录路径
        
        int cnt = 1 << (cityCount - 1);
        long dp[][] = new long [cityCount][ 1 << (cityCount - 1)];
        for(int i = 0;i < cityCount;i++){
            for(int j = 0;j < cityCount;j++){    
                dp[i][j] = 0x3f3f3f3f;                        //用0x7ffff表示无穷大
            }
        }
        for (int i=0;i<cityCount;i++) {
        	dp[i][0] = roadmap[i][0];
        }
        for (int i=1;i<cnt-1;i++) {//从1开始，保证初始点不在里面
        	for (int j=1;j<cityCount;j++) {
        		if((i&(1<<(j-1)))==1)continue;
        		long minn=0x3f3f3f3f;
        		for (int k=1;k<cityCount;k++) {
        			if (((1 << (k - 1))&i) >0) {
        				long tmp = roadmap[j][k]+dp[k][i^(1<<(k-1))];
        				if (tmp<minn) {
        					dp[j][i] = tmp;
        					minn = tmp;
        					pos[j][i]=k; 
        				}
        			}
        		}
        	}
        }
        int minn=0x3f3f3f3f;
        for(int k=1;k<cityCount;k++){
            int tmp=(int) (dp[k][(cnt-1)^(1<<(k-1))]+roadmap[k][0]);
            if(tmp<minn) {
            	minn=tmp;
            	dp[0][cnt-1]=tmp;
            	pos[0][cnt-1]=k;
            }
        }
        System.out.println("minn="+dp[cityCount-1][0]);
        
        System.out.print("0");  //打印路径
        for(int i=(1 << (cityCount - 1))-1, next=0;i>0;){
            next=pos[next][i];
            System.out.print("->"+next);
            i=i^(1<<(next-1));
        }
        System.out.println("->0");
        return dp[cityCount-1][0];
	}
	
	public long[][] inputDistance() {
		String filename = "F:\\迅雷下载\\TSP_dataset\\test.txt";
		String strbuff;
		long[][] dist = null;
		int n;
		try {
			BufferedReader data = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename)));
			n= Integer.valueOf(data.readLine());
			cityNum=n;
			dist = new long[n][n];
			int index=0;
			while ((strbuff = data.readLine())!=null) {
				String[] tmp = strbuff.split(" ");
				for (int i=0;i<tmp.length;i++) {
					if (i==index) {
						dist[index][i] = 1000;
						for (int k=i;k<tmp.length;k++) {
							dist[index][k+1] =  Long.valueOf(tmp[k]) ;
						}
						break;
					}
					else 
						dist[index][i]= Long.valueOf(tmp[i]) ;
				}
				index=index+1;
			}
			dist[n-1][n-1]=1000;
		} catch (NumberFormatException | IOException e) {
			System.out.println("文件不存在！！！");
			e.printStackTrace();
		}
		printArray(dist);
		return dist;
	}
	
	public void printArray(long[][] a) {
		System.out.println("====array=====");
		for (int i=0;i<a.length;i++) {
			for (int j=0;j<a[i].length;j++) {
				System.out.print(a[i][j]+" ");
			}
			System.out.println();
		}
		System.out.println("=============");
	}


	public static void main(String[] args) throws IOException {
		
		System.out.println("Start....");
		DP_TSP ts = new DP_TSP(23);
		ts.init("resources/eil23.txt");
//		ts.distance = ts.inputDistance();
		ts.DP();
	}
}
