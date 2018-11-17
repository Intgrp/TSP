import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TabuSearchAlgorithm_TSP {
	/** 迭代次数 */
	private int MAX_GEN;
	/** 每次搜索邻居个数 */
	private int neighbourhoodNum;
	/** 禁忌长度 */
	private int tabuTableLength;
	/** 节点数量，编码长度 */
	private int nodeNum;
	/** 节点间距离矩阵 */
	private int[][] nodeDistance;
	/** 当前路线 */
	private int[] route;
	/** 最好的路径 */
	public int[] bestRoute;
	/** 最佳路径总长度 */
	private int bestEvaluation;
	/** 禁忌表 */
	private int[][] tabuTable;
	/** 禁忌表中的评估值 */
	private int[] tabuTableEvaluate;

	private long tp;

	public TabuSearchAlgorithm_TSP() {

	}

	/**
	 * constructor of GA
	 * 
	 * @param n
	 *            城市数量
	 * @param g
	 *            运行代数
	 * @param c
	 *            每次搜索邻居个数
	 * @param m
	 *            禁忌长度
	 * 
	 **/
	public TabuSearchAlgorithm_TSP(int n, int g, int c, int m) {
		nodeNum = n;
		MAX_GEN = g;
		neighbourhoodNum = c;
		tabuTableLength = m;
	}

	/**
	 * 初始化Tabu算法类
	 * 
	 * @param filename
	 *            数据文件名，该文件存储所有城市节点坐标数据
	 * @throws IOException
	 */
	public void init(String filename) throws IOException {
		// 读取数据
		int[] x;
		int[] y;
		String strbuff;
		BufferedReader data = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		nodeDistance = new int[nodeNum][nodeNum];
		x = new int[nodeNum];
		y = new int[nodeNum];
		while ((strbuff = data.readLine())!=null) {
			if (!Character.isAlphabetic(strbuff.charAt(0)))
				break;
		}
		String[] tmp = strbuff.split(" ");
		x[0] = Integer.valueOf(tmp[1]);// x坐标
		y[0] = Integer.valueOf(tmp[2]);// y坐标
		for (int i = 1; i < nodeNum; i++) {
			strbuff = data.readLine();
			String[] strcol = strbuff.split(" ");
			x[i] = Integer.valueOf(strcol[1]).intValue();
			y[i] = Integer.valueOf(strcol[2]).intValue();
		}
//		String[] strcol;
//		for (int i = 0; i < nodeNum; i++) {
//			// 读取一行数据，数据格式1 6734 1453
//			strbuff = list.get(i);
//			// 字符分割
//			strcol = strbuff.split(" ");
//			x[i] = Integer.valueOf(strcol[1]);// x坐标
//			y[i] = Integer.valueOf(strcol[2]);// y坐标
//		}
		// 计算距离矩阵
		// ，针对具体问题，距离计算方法也不一样，此处用的是att48作为案例，它有48个城市，距离计算方法为伪欧氏距离，最优值为10628
		for (int i = 0; i < nodeNum - 1; i++) {
			nodeDistance[i][i] = 0; // 对角线为0
			for (int j = i + 1; j < nodeNum; j++) {
				double rij = Math.sqrt(((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i] - y[j])) / 10.0);
				// 四舍五入，取整
				int tij = (int) Math.round(rij);
				if (tij < rij) {
					nodeDistance[i][j] = tij + 1;
					nodeDistance[j][i] = nodeDistance[i][j];
				} else {
					nodeDistance[i][j] = tij;
					nodeDistance[j][i] = nodeDistance[i][j];
				}
			}
		}
		nodeDistance[nodeNum - 1][nodeNum - 1] = 0;

		route = new int[nodeNum];
		bestRoute = new int[nodeNum];
		bestEvaluation = Integer.MAX_VALUE;
		tabuTable = new int[tabuTableLength][nodeNum];
		tabuTableEvaluate = new int[tabuTableLength];
		for (int i = 0; i < tabuTableEvaluate.length; i++) {
			tabuTableEvaluate[i] = Integer.MAX_VALUE;
		}
	}

	/** 生成初始群体 */
	void generateInitGroup() {
		System.out.println("1.生成初始群体");
		boolean iscontinue = false;
		for (int i = 0; i < route.length; i++) {
			do {
				iscontinue = false;
				route[i] = (int) (Math.random() * nodeNum);
				for (int j = i - 1; j >= 0; j--) {
					if (route[i] == route[j]) {
						iscontinue = true;
						break;
					}
				}
			} while (iscontinue);
			// System.out.println("i="+i+", route[i]="+route[i]);
		}
	}

	/** 复制编码体，复制Gha到Ghb */
	public void copyGh(int[] Gha, int[] Ghb) {
		for (int i = 0; i < nodeNum; i++) {
			Ghb[i] = Gha[i];
		}
	}

	/** 计算路线的总距离 */
	public int evaluate(int[] chr) {
		// 0123
		int len = 0;
		// 编码，起始城市,城市1,城市2...城市n
		for (int i = 1; i < nodeNum; i++) {
			len += nodeDistance[chr[i - 1]][chr[i]];
		}
		// 城市n,起始城市
		len += nodeDistance[chr[nodeNum - 1]][chr[0]];
		return len;
	}

	/**
	 * 随机获取邻域路径
	 * 
	 * @param route
	 *            当前路径
	 */
	public int[] getNeighbourhood(int[] route) {
		int temp;
		int ran1, ran2;
		int[] tempRoute = new int[route.length];
		copyGh(route, tempRoute);
		ran1 = (int) (Math.random() * nodeNum);
		do {
			ran2 = (int) (Math.random() * nodeNum);
		} while (ran1 == ran2);
		temp = tempRoute[ran1];
		tempRoute[ran1] = tempRoute[ran2];
		tempRoute[ran2] = temp;
		return tempRoute;
	}

	/**
	 * 随机获取一定数量的领域路径
	 */
	public int[][] getNeighbourhood(int[] route, int tempNeighbourhoodNum) {
		int[][] NeighbourhoodRoutes = new int[tempNeighbourhoodNum][nodeNum];
		List<int[]> tempExchangeNodeList = new ArrayList<>();
		int temp;
		int ran0, ran1;
		int[] tempRoute = null;
		boolean iscontinue;
		for (int i = 0; i < tempNeighbourhoodNum; i++) {
			tempRoute = new int[route.length];
			copyGh(route, tempRoute);
			do {
				iscontinue = false;
				// 随机生成一个邻域;
				ran0 = (int) (Math.random() * nodeNum);
				do {
					ran1 = (int) (Math.random() * nodeNum);
				} while (ran0 == ran1);
				// 判断是否重复
				for (int j = 0; j < tempExchangeNodeList.size(); j++) {
					if (tempExchangeNodeList.get(j)[0] < tempExchangeNodeList.get(j)[1]) {
						if ((ran0 < ran1
								&& (tempExchangeNodeList.get(j)[0] == ran0 && tempExchangeNodeList.get(j)[1] == ran1))
								|| (ran0 > ran1 && (tempExchangeNodeList.get(j)[0] == ran1
										&& tempExchangeNodeList.get(j)[1] == ran0))) {
							iscontinue = true;
						}
					} else {
						if ((ran0 < ran1
								&& (tempExchangeNodeList.get(j)[0] == ran1 && tempExchangeNodeList.get(j)[1] == ran0))
								|| (ran0 > ran1 && (tempExchangeNodeList.get(j)[0] == ran0
										&& tempExchangeNodeList.get(j)[1] == ran1))) {
							iscontinue = true;
						}
					}
				}
				if (iscontinue == false) {
					temp = tempRoute[ran0];
					tempRoute[ran0] = tempRoute[ran1];
					tempRoute[ran1] = temp;
					tempExchangeNodeList.add(new int[]{ran0,ran1});//将交换点添加到列表中用于查重；
					// 判断是否与route相同
					for (int j = 0; j < tempRoute.length; j++) {
						if (tempRoute[j] != route[j]) {
							iscontinue = false;
						}
					}
					if (iscontinue == false && !isInTabuTable(tempRoute)) {
						NeighbourhoodRoutes[i] = tempRoute;
					} else {
						iscontinue = true;
					}
				}
			} while (iscontinue);
		}
		return NeighbourhoodRoutes;
	}

	/** 判断路径是否在禁忌表中 */
	public boolean isInTabuTable(int[] tempRoute) {
		int i, j;
		int flag = 0;
		for (i = 0; i < tabuTableLength; i++) {
			flag = 0;
			for (j = 0; j < nodeNum; j++) {
				if (tempRoute[j] != tabuTable[i][j]) {
					flag = 1;// 不相同
					break;
				}
			}
			if (flag == 0) {// 相同，返回存在相同
				break;
			}
		}
		if (i == tabuTableLength) {// 不等
			return false;// 不存在
		} else {
			return true;// 存在
		}
	}

	/** 解禁忌与加入禁忌，注意禁忌策略的选择 */
	public void flushTabuTable(int[] tempGh) {
		int tempValue = evaluate(tempGh);
		// 找到禁忌表中路径的最大值；
		int tempMax = tabuTableEvaluate[0];
		int maxValueIndex = 0;
		for (int i = 0; i < tabuTableLength; i++) {
			if (tabuTableEvaluate[i] > tempMax) {
				tempMax = tabuTableEvaluate[i];
				maxValueIndex = i;
			}
		}
		// 新的路径加入禁忌表
		if (tempValue < tabuTableEvaluate[maxValueIndex]) {
			if (tabuTableEvaluate[maxValueIndex] < Integer.MAX_VALUE) {
				copyGh(tabuTable[maxValueIndex], route);
			}
			System.out.println("测试点：更新禁忌表，maxValueIndex= " + maxValueIndex);
			for (int k = 0; k < nodeNum; k++) {
				tabuTable[maxValueIndex][k] = tempGh[k];
			}
			tabuTableEvaluate[maxValueIndex] = tempValue;
		}
	}

	/** 启动禁忌搜索 */
	public void startSearch() {
		int nn;
		int neighbourhoodEvaluation;
		int currentBestRouteEvaluation;
		/** 存放邻域路径 */
		int[] neighbourhoodOfRoute = new int[nodeNum];
		/** 当代最好路径 */
		int[] currentBestRoute = new int[nodeNum];
		/** 当前代数 */
		int currentIterateNum = 0;
		/** 最佳出现代数 */
		int bestIterateNum = 0;
		int[][] neighbourhoodOfRoutes = null;
		// 用于控制迭代次数
		int[] priviousRoute = new int[nodeNum];
		// 初始化编码Ghh
		generateInitGroup();
		// 将当前路径作为最好路径
		copyGh(route, bestRoute);
		currentBestRouteEvaluation = evaluate(route);
		bestEvaluation = currentBestRouteEvaluation;
		System.out.println("2.迭代搜索....");
		while (currentIterateNum < MAX_GEN) {
			for (int i = 0; i < route.length; i++) {
				priviousRoute[i] = route[i];
			}
			neighbourhoodOfRoutes = getNeighbourhood(route, neighbourhoodNum);
			System.out.println("测试点：currentIterateNum= " + currentIterateNum);
			for (nn = 0; nn < neighbourhoodNum; nn++) {
				// 得到当前路径route的一个邻域路径neighbourhoodOfRoute
				// neighbourhoodOfRoute=getNeighbourhood(route);
				neighbourhoodOfRoute = neighbourhoodOfRoutes[nn];
				neighbourhoodEvaluation = evaluate(neighbourhoodOfRoute);
				// System.out.println("测试：neighbourhoodOfRoute="+neighbourhoodEvaluation);
				if (neighbourhoodEvaluation < currentBestRouteEvaluation) {
					copyGh(neighbourhoodOfRoute, currentBestRoute);
					currentBestRouteEvaluation = neighbourhoodEvaluation;
					// System.out.println("测试：neighbourhoodOfRoute="+neighbourhoodEvaluation);
				}
			}
			if (currentBestRouteEvaluation < bestEvaluation) {
				bestIterateNum = currentIterateNum;
				copyGh(currentBestRoute, bestRoute);
				bestEvaluation = currentBestRouteEvaluation;
				System.out.println("测试：currentBestRouteEvaluation=" + currentBestRouteEvaluation);
			}
			copyGh(currentBestRoute, route);
			// 解禁忌表，currentBestRoute加入禁忌表
			// System.out.println("测试点：currentBestRoute= "+currentBestRoute);
			flushTabuTable(currentBestRoute);
			currentIterateNum++;
			for (int i = 0; i < priviousRoute.length; i++) {
				if (priviousRoute[i] != route[i]) {
					currentIterateNum = 0;
					break;
				}
			}

			printRunStatus();
		}

		// 结果显示：
		System.out.println("最佳长度出现代数：");
		System.out.println(bestIterateNum);
		System.out.println("最佳长度:");
		System.out.println(bestEvaluation);
		System.out.println("最佳路径：");
		for (int i = 0; i < nodeNum; i++) {
			System.out.print(bestRoute[i] + ",");
		}
	}

	/**
	 * @Description: 输出结运行状态
	 */
	private void printRunStatus() {
		System.out.println("最优路径长度：" + bestEvaluation);
	}

	public static void main(String[] args) throws IOException {
		System.out.println("Start....");
		List<String> listSearch = new ArrayList<String>();
		TabuSearchAlgorithm_TSP tabu = new TabuSearchAlgorithm_TSP(48, 120, 500, 5);
		tabu.init("resources/att48.tsp");
		tabu.startSearch();
	}
}