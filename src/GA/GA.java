package GA;

import java.io.BufferedReader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GA {

	private Chromosome[] chromosomes;
	private Chromosome[] nextGeneration;
	private int N;
	private int cityNum;
	private double p_c_t;
	private double p_m_t;
	private int MAX_GEN;
	private int bestLength;
	private int[] bestTour;
	private double bestFitness;
	private double[] averageFitness;
	private int[][] distance;
	private String filename;

	public GA() {
		N = 100;
		cityNum = 30;
		p_c_t = 0.9;
		p_m_t = 0.1;
		MAX_GEN = 1000;
		bestLength = 0;
		bestTour = new int[cityNum];
		bestFitness = 0.0;
		averageFitness = new double[MAX_GEN];
		chromosomes = new Chromosome[N];
		distance = new int[cityNum][cityNum];

	}

	/**
	 * Constructor of GA class
	 * 
	 * @param n
	 *            种群规模
	 * @param num
	 *            城市规模
	 * @param g
	 *            运行代数
	 * @param p_c
	 *            交叉率
	 * @param p_m
	 *            变异率
	 * @param filename
	 *            数据文件名
	 */
	public GA(int n, int num, int g, double p_c, double p_m, String filename) {
		this.N = n;
		this.cityNum = num;
		this.MAX_GEN = g;
		this.p_c_t = p_c;
		this.p_m_t = p_m;
		bestTour = new int[cityNum];
		averageFitness = new double[MAX_GEN];
		bestFitness = 0.0;
		chromosomes = new Chromosome[N];
		nextGeneration = new Chromosome[N];
		distance = new int[cityNum][cityNum];
		this.filename = filename;
	}

	public void solve() throws IOException {
		System.out.println("---------------------Start initilization---------------------");
		init();
		System.out.println("---------------------End initilization---------------------");
		System.out.println("---------------------Start evolution---------------------");
		for (int i = 0; i < MAX_GEN; i++) {
			System.out.println("-----------Start generation " + i + "----------");
			evolve(i);
			System.out.println("-----------End generation " + i + "----------");
		}
		System.out.println("---------------------End evolution---------------------");
		printOptimal();
		outputResults();

	}

	/**
	 * 初始化GA
	 * 
	 * @throws IOException
	 */
	@SuppressWarnings("resource")
	private void init() throws IOException {
		// 读取数据文件
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
		x[0] = Integer.valueOf(tmp[1]);// x坐标
		y[0] = Integer.valueOf(tmp[2]);// y坐标
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
				double rij = Math.sqrt((x[i] - x[j]) * (x[i] - x[j]) + (y[i] - y[j]) * (y[i] - y[j]));
				int tij = (int) Math.round(rij);
				// if (tij < rij) {
				distance[i][j] = tij;
				distance[j][i] = distance[i][j];
				/*
				 * }else { distance[i][j] = tij; distance[j][i] = distance[i][j]; }
				 */
			}
		}
		distance[cityNum - 1][cityNum - 1] = 0;

		for (int i = 0; i < N; i++) {
			Chromosome chromosome = new Chromosome(cityNum, distance);
			chromosome.randomGeneration();
			chromosomes[i] = chromosome;
			chromosome.print();
		}
	}

	private void evolve(int g) {
		double[] selectionP = new double[N];// 选择概率
		double sum = 0.0;
		double tmp = 0.0;

		for (int i = 0; i < N; i++) {
			sum += chromosomes[i].getFitness();
			if (chromosomes[i].getFitness() > bestFitness) {
				bestFitness = chromosomes[i].getFitness();
				bestLength = (int) (1.0 / bestFitness);
				for (int j = 0; j < cityNum; j++) {
					bestTour[j] = chromosomes[i].getTour()[j];
				}

			}
		}
		averageFitness[g] = sum / N;

		System.out.println("The average fitness in " + g + " generation is: " + averageFitness[g]
				+ ", and the best fitness is: " + bestFitness);
		for (int i = 0; i < N; i++) {
			tmp += chromosomes[i].getFitness() / sum;
			selectionP[i] = tmp;
		}
		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < N; i = i + 2) {

			Chromosome[] children = new Chromosome[2];
			// 轮盘赌选择两个染色体
			// System.out.println("---------start selection-----------");
			// System.out.println();
			for (int j = 0; j < 2; j++) {

				int selectedCity = 0;
				for (int k = 0; k < N - 1; k++) {
					double p = random.nextDouble();
					if (p > selectionP[k] && p <= selectionP[k + 1]) {
						selectedCity = k;
					}
					if (k == 0 && random.nextDouble() <= selectionP[k]) {
						selectedCity = 0;
					}
				}
				try {
					children[j] = (Chromosome) chromosomes[selectedCity].clone();

					// children[j].print();
					// System.out.println();
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// 交叉操作(OX1)

			// System.out.println("----------Start crossover----------");
			// System.out.println();
			// Random random = new Random(System.currentTimeMillis());
			if (random.nextDouble() < p_c_t) {
				// System.out.println("crossover");
				// random = new Random(System.currentTimeMillis());
				// 定义两个cut点
				int cutPoint1 = -1;
				int cutPoint2 = -1;
				int r1 = random.nextInt(cityNum);
				if (r1 > 0 && r1 < cityNum - 1) {
					cutPoint1 = r1;
					// random = new Random(System.currentTimeMillis());
					int r2 = random.nextInt(cityNum - r1);
					if (r2 == 0) {
						cutPoint2 = r1 + 1;
					} else if (r2 > 0) {
						cutPoint2 = r1 + r2;
					}

				}
				if (cutPoint1 > 0 && cutPoint2 > 0) {
					// System.out.println("Cut point1 is: "+cutPoint1 +", and cut point2 is:
					// "+cutPoint2);
					int[] tour1 = new int[cityNum];
					int[] tour2 = new int[cityNum];
					if (cutPoint2 == cityNum - 1) {
						for (int j = 0; j < cityNum; j++) {
							tour1[j] = children[0].getTour()[j];
							tour2[j] = children[1].getTour()[j];
						}
					} else {

						// int n = 1;
						for (int j = 0; j < cityNum; j++) {
							if (j < cutPoint1) {
								tour1[j] = children[0].getTour()[j];
								tour2[j] = children[1].getTour()[j];
							} else if (j >= cutPoint1 && j < cutPoint1 + cityNum - cutPoint2 - 1) {
								tour1[j] = children[0].getTour()[j + cutPoint2 - cutPoint1 + 1];
								tour2[j] = children[1].getTour()[j + cutPoint2 - cutPoint1 + 1];
							} else {
								tour1[j] = children[0].getTour()[j - cityNum + cutPoint2 + 1];
								tour2[j] = children[1].getTour()[j - cityNum + cutPoint2 + 1];
							}

						}
					}
					/*
					 * System.out.println("The two tours are: "); for (int j = 0; j < cityNum; j++)
					 * { System.out.print(tour1[j] +"\t"); } System.out.println(); for (int j = 0; j
					 * < cityNum; j++) { System.out.print(tour2[j] +"\t"); } System.out.println();
					 */

					for (int j = 0; j < cityNum; j++) {
						if (j < cutPoint1 || j > cutPoint2) {

							children[0].getTour()[j] = -1;
							children[1].getTour()[j] = -1;
						} else {
							int tmp1 = children[0].getTour()[j];
							children[0].getTour()[j] = children[1].getTour()[j];
							children[1].getTour()[j] = tmp1;
						}
					}
					/*
					 * for (int j = 0; j < cityNum; j++) {
					 * System.out.print(children[0].getTour()[j]+"\t"); } System.out.println(); for
					 * (int j = 0; j < cityNum; j++) {
					 * System.out.print(children[1].getTour()[j]+"\t"); } System.out.println();
					 */
					if (cutPoint2 == cityNum - 1) {
						int position = 0;
						for (int j = 0; j < cutPoint1; j++) {
							for (int m = position; m < cityNum; m++) {
								boolean flag = true;
								for (int n = 0; n < cityNum; n++) {
									if (tour1[m] == children[0].getTour()[n]) {
										flag = false;
										break;
									}
								}
								if (flag) {

									children[0].getTour()[j] = tour1[m];
									position = m + 1;
									break;
								}
							}
						}
						position = 0;
						for (int j = 0; j < cutPoint1; j++) {
							for (int m = position; m < cityNum; m++) {
								boolean flag = true;
								for (int n = 0; n < cityNum; n++) {
									if (tour2[m] == children[1].getTour()[n]) {
										flag = false;
										break;
									}
								}
								if (flag) {
									children[1].getTour()[j] = tour2[m];
									position = m + 1;
									break;
								}
							}
						}

					} else {

						int position = 0;
						for (int j = cutPoint2 + 1; j < cityNum; j++) {
							for (int m = position; m < cityNum; m++) {
								boolean flag = true;
								for (int n = 0; n < cityNum; n++) {
									if (tour1[m] == children[0].getTour()[n]) {
										flag = false;
										break;
									}
								}
								if (flag) {
									children[0].getTour()[j] = tour1[m];
									position = m + 1;
									break;
								}
							}
						}
						for (int j = 0; j < cutPoint1; j++) {
							for (int m = position; m < cityNum; m++) {
								boolean flag = true;
								for (int n = 0; n < cityNum; n++) {
									if (tour1[m] == children[0].getTour()[n]) {
										flag = false;
										break;
									}
								}
								if (flag) {
									children[0].getTour()[j] = tour1[m];
									position = m + 1;
									break;
								}
							}
						}

						position = 0;
						for (int j = cutPoint2 + 1; j < cityNum; j++) {
							for (int m = position; m < cityNum; m++) {
								boolean flag = true;
								for (int n = 0; n < cityNum; n++) {
									if (tour2[m] == children[1].getTour()[n]) {
										flag = false;
										break;
									}
								}
								if (flag) {
									children[1].getTour()[j] = tour2[m];
									position = m + 1;
									break;
								}
							}
						}
						for (int j = 0; j < cutPoint1; j++) {
							for (int m = position; m < cityNum; m++) {
								boolean flag = true;
								for (int n = 0; n < cityNum; n++) {
									if (tour2[m] == children[1].getTour()[n]) {
										flag = false;
										break;
									}
								}
								if (flag) {
									children[1].getTour()[j] = tour2[m];
									position = m + 1;
									break;
								}
							}
						}
					}

				}
			}
			// children[0].print();
			// children[1].print();

			// 变异操作(DM)

			// System.out.println("---------Start mutation------");
			// System.out.println();
			// random = new Random(System.currentTimeMillis());
			if (random.nextDouble() < p_m_t) {
				// System.out.println("mutation");
				for (int j = 0; j < 2; j++) {
					// random = new Random(System.currentTimeMillis());
					// 定义两个cut点
					int cutPoint1 = -1;
					int cutPoint2 = -1;
					int r1 = random.nextInt(cityNum);
					if (r1 > 0 && r1 < cityNum - 1) {
						cutPoint1 = r1;
						// random = new Random(System.currentTimeMillis());
						int r2 = random.nextInt(cityNum - r1);
						if (r2 == 0) {
							cutPoint2 = r1 + 1;
						} else if (r2 > 0) {
							cutPoint2 = r1 + r2;
						}

					}

					if (cutPoint1 > 0 && cutPoint2 > 0) {
						List<Integer> tour = new ArrayList<Integer>();
						// System.out.println("Cut point1 is "+cutPoint1+", and cut point2 is
						// "+cutPoint2);
						if (cutPoint2 == cityNum - 1) {
							for (int k = 0; k < cutPoint1; k++) {
								tour.add(Integer.valueOf(children[j].getTour()[k]));
							}
						} else {
							for (int k = 0; k < cityNum; k++) {
								if (k < cutPoint1 || k > cutPoint2) {
									tour.add(Integer.valueOf(children[j].getTour()[k]));
								}
							}
						}
						// random = new Random(System.currentTimeMillis());
						int position = random.nextInt(tour.size());

						if (position == 0) {

							for (int k = cutPoint2; k >= cutPoint1; k--) {
								tour.add(0, Integer.valueOf(children[j].getTour()[k]));
							}

						} else if (position == tour.size() - 1) {

							for (int k = cutPoint1; k <= cutPoint2; k++) {
								tour.add(Integer.valueOf(children[j].getTour()[k]));
							}

						} else {

							for (int k = cutPoint1; k <= cutPoint2; k++) {
								tour.add(position, Integer.valueOf(children[j].getTour()[k]));
							}

						}

						for (int k = 0; k < cityNum; k++) {
							children[j].getTour()[k] = tour.get(k).intValue();

						}
						// System.out.println();
					}

				}
			}

			// children[0].print();
			// children[1].print();

			nextGeneration[i] = children[0];
			nextGeneration[i + 1] = children[1];

		}

		for (int k = 0; k < N; k++) {
			try {
				chromosomes[k] = (Chromosome) nextGeneration[k].clone();

			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		/*
		 * System.out.println("Next generation is:"); for (int k = 0; k < N; k++) {
		 * chromosomes[k].print(); }
		 */
	}

	private void printOptimal() {
		System.out.println("The best fitness is: " + bestFitness);
		System.out.println("The best tour length is: " + bestLength);
		System.out.println("The best tour is: ");
		
		System.out.print(bestTour[0] );
		for (int i = 1; i < cityNum; i++) {
			System.out.print("->"+bestTour[i]);
		}
		System.out.println();
	}

	private void outputResults() {
		String filename = "result.txt";
		/*
		 * File file = new File(filename); if (!file.exists()) { try {
		 * file.createNewFile(); } catch (IOException e) { // TODO Auto-generated catch
		 * block e.printStackTrace(); } }
		 */
		try {
			@SuppressWarnings("resource")
			FileOutputStream outputStream = new FileOutputStream(filename);
			for (int i = 0; i < averageFitness.length; i++) {
				String line = String.valueOf(averageFitness[i]) + "\r\n";

				outputStream.write(line.getBytes());

			}

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public Chromosome[] getChromosomes() {
		return chromosomes;
	}

	public void setChromosomes(Chromosome[] chromosomes) {
		this.chromosomes = chromosomes;
	}

	public int getCityNum() {
		return cityNum;
	}

	public void setCityNum(int cityNum) {
		this.cityNum = cityNum;
	}

	public double getP_c_t() {
		return p_c_t;
	}

	public void setP_c_t(double p_c_t) {
		this.p_c_t = p_c_t;
	}

	public double getP_m_t() {
		return p_m_t;
	}

	public void setP_m_t(double p_m_t) {
		this.p_m_t = p_m_t;
	}

	public int getMAX_GEN() {
		return MAX_GEN;
	}

	public void setMAX_GEN(int mAX_GEN) {
		MAX_GEN = mAX_GEN;
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

	public double[] getAverageFitness() {
		return averageFitness;
	}

	public void setAverageFitness(double[] averageFitness) {
		this.averageFitness = averageFitness;
	}

	public int getN() {
		return N;
	}

	public void setN(int n) {
		N = n;
	}

	public int[][] getDistance() {
		return distance;
	}

	public void setDistance(int[][] distance) {
		this.distance = distance;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		GA ga = new GA(100, 51, 100, 0.95, 0.75, "resources/eil51.txt");
		ga.solve();
	}

}