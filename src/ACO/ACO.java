package ACO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * @author BIAO YU
 * 
 *
 */
public class ACO {

	private Ant[] ants; // 蚂蚁
	private int antNum; // 蚂蚁数量
	private int cityNum; // 城市数量
	private int MAX_GEN; // 运行代数
	private float[][] pheromone; // 信息素矩阵
	private int[][] distance; // 距离矩阵
	private int bestLength; // 最佳长度
	private int[] bestTour; // 最佳路径

	// 三个参数
	private float alpha;
	private float beta;
	private float rho;

	public ACO() {

	}

	/**
	 * constructor of ACO
	 * 
	 * @param n
	 *            城市数量
	 * @param m
	 *            蚂蚁数量
	 * @param g
	 *            运行代数
	 * @param a
	 *            alpha
	 * @param b
	 *            beta
	 * @param r
	 *            rho
	 * 
	 **/
	public ACO(int n, int m, int g, float a, float b, float r) {
		cityNum = n;
		antNum = m;
		ants = new Ant[antNum];
		MAX_GEN = g;
		alpha = a;
		beta = b;
		rho = r;

	}

	@SuppressWarnings("resource")
	/**
	 * 初始化ACO算法类
	 * 
	 * @param filename
	 *            数据文件名，该文件存储所有城市节点坐标数据
	 * @throws IOException
	 */
	private void init(String filename) throws IOException {
		// 读取数据
		int[] x;
		int[] y;
		String strbuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		
		distance = new int[cityNum][cityNum];
		x = new int[cityNum];
		y = new int[cityNum];
		while ((strbuff = data.readLine())!=null) {
			if (!Character.isAlphabetic(strbuff.charAt(0)))
				break;
		}
		String[] tmp = strbuff.split(" ");
		x[0] = Integer.valueOf(tmp[1]).intValue();// x坐标
		y[0] = Integer.valueOf(tmp[2]).intValue();// y坐标
		
		for (int i = 1; i < cityNum; i++) {
			strbuff = data.readLine();
			String[] strcol = strbuff.split(" ");
			x[i] = Integer.valueOf(strcol[1]).intValue();
			y[i] = Integer.valueOf(strcol[2]).intValue();
		}
		// 计算距离矩阵 ，针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
		for (int i = 0; i < cityNum - 1; i++) {
			distance[i][i] = 0; // 对角线为0
			for (int j = i + 1; j < cityNum; j++) {
				double rij = Math.sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i] - y[j])) / 10.0);
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

		// 初始化信息素矩阵
		pheromone = new float[cityNum][cityNum];
		for (int i = 0; i < cityNum; i++) {
			for (int j = 0; j < cityNum; j++) {
				pheromone[i][j] = 0.1f; // 初始化为0.1
			}
		}
		bestLength = Integer.MAX_VALUE;
		bestTour = new int[cityNum + 1];
		// 随机放置蚂蚁
		for (int i = 0; i < antNum; i++) {
			ants[i] = new Ant(cityNum);
			ants[i].init(distance, alpha, beta);
		}
	}

	public void solve() {

		for (int g = 0; g < MAX_GEN; g++) {
			for (int i = 0; i < antNum; i++) {
				for (int j = 1; j < cityNum; j++) {
					ants[i].selectNextCity(pheromone);
				}
				ants[i].getTabu().add(ants[i].getFirstCity());
				if (ants[i].getTourLength() < bestLength) {
					bestLength = ants[i].getTourLength();
					for (int k = 0; k < cityNum + 1; k++) {
						bestTour[k] = ants[i].getTabu().get(k).intValue();
					}
				}
				for (int j = 0; j < cityNum; j++) {
					ants[i].getDelta()[ants[i].getTabu().get(j).intValue()][ants[i].getTabu().get(j + 1)
							.intValue()] = (float) (1. / ants[i].getTourLength());
					ants[i].getDelta()[ants[i].getTabu().get(j + 1).intValue()][ants[i].getTabu().get(j)
							.intValue()] = (float) (1. / ants[i].getTourLength());
				}
			}

			// 更新信息素
			updatePheromone();

			// 重新初始化蚂蚁
			for (int i = 0; i < antNum; i++) {

				ants[i].init(distance, alpha, beta);
			}
		}

		// 打印最佳结果
		printOptimal();
	}

	// 更新信息素
	private void updatePheromone() {
		// 信息素挥发
		for (int i = 0; i < cityNum; i++)
			for (int j = 0; j < cityNum; j++)
				pheromone[i][j] = pheromone[i][j] * (1 - rho);
		// 信息素更新
		for (int i = 0; i < cityNum; i++) {
			for (int j = 0; j < cityNum; j++) {
				for (int k = 0; k < antNum; k++) {
					pheromone[i][j] += ants[k].getDelta()[i][j];
				}
			}
		}
	}

	private void printOptimal() {
		System.out.println("The optimal length is: " + bestLength);
		System.out.println("The optimal tour is: ");
		System.out.print(bestTour[0]);
		for (int i = 0; i < cityNum + 1; i++) {
			System.out.print("->"+bestTour[i]);
		}
	}

	public Ant[] getAnts() {
		return ants;
	}

	public void setAnts(Ant[] ants) {
		this.ants = ants;
	}

	public int getAntNum() {
		return antNum;
	}

	public void setAntNum(int m) {
		this.antNum = m;
	}

	public int getCityNum() {
		return cityNum;
	}

	public void setCityNum(int cityNum) {
		this.cityNum = cityNum;
	}

	public int getMAX_GEN() {
		return MAX_GEN;
	}

	public void setMAX_GEN(int mAX_GEN) {
		MAX_GEN = mAX_GEN;
	}

	public float[][] getPheromone() {
		return pheromone;
	}

	public void setPheromone(float[][] pheromone) {
		this.pheromone = pheromone;
	}

	public int[][] getDistance() {
		return distance;
	}

	public void setDistance(int[][] distance) {
		this.distance = distance;
	}

	public int getBestLength() {
		return bestLength;
	}

	public void setBestLength(int bestLength) {
		this.bestLength = bestLength;
	}

	public int[] getBestTour() {
		return bestTour;
	}

	public void setBestTour(int[] bestTour) {
		this.bestTour = bestTour;
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getBeta() {
		return beta;
	}

	public void setBeta(float beta) {
		this.beta = beta;
	}

	public float getRho() {
		return rho;
	}

	public void setRho(float rho) {
		this.rho = rho;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		ACO aco = new ACO(48, 100, 1000, 1.f, 5.f, 0.5f);
		aco.init("resources/att48.tsp");
		aco.solve();
	}

}