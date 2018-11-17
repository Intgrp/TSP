import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

import javax.management.Query;
import javax.swing.text.StyledEditorKit.ForegroundAction;

import util.TspProblem;
import util.TspReader;

public class SA {

	TspProblem problem;

	public SA(TspProblem problem) {
		this.problem = problem;
	}

	/**
	 * 广度优先搜索，输出贪心出来的最优路径，即每次都从起始点找附近最进的点去贪心遍历
	 * @return 输出得出的遍历节点顺序
	 */
	public int[] BFS() {
		Queue<Integer> q = new LinkedList<>();
		q.add(0);
		int[] vis = new int[problem.getxCoors().length];
		int[] out = new int[problem.getxCoors().length];
		vis[0] = 1;
		int totalDist = 0;
		int index = 1;
		while (!q.isEmpty()) {
			int front = q.poll();
			int min = Integer.MAX_VALUE;
			int sIdx = 0;
			for (int i = 0; i < problem.getxCoors().length; i++) {
				if (vis[i] == 0 && i != front && min > problem.getDistance()[front][i]) {
					min = problem.getDistance()[front][i];
					sIdx = i;
				}
			}
			if (min != Integer.MAX_VALUE) {
				vis[sIdx] = 1;
				q.add(sIdx);
				out[index] = sIdx;
				index++;
				totalDist += problem.getDistance()[front][sIdx];
			}
		}
		q = null;
		totalDist+=problem.getDistance()[out[out.length-1]][0];
		return out;
	}

	public int cost(int[] rout) {
		int sum = 0;
		int[][] dist = problem.getDistance();
		for (int i = 0; i < rout.length-1; i++) {
			sum += dist[ rout[i] ][ rout[i + 1] ];
		}
		sum+=dist[rout[rout.length - 1]][rout[0]];
		return sum;
	}

	public int[] copyRout(int[] rout) {
		int[] out = new int[rout.length];
		for (int i = 0; i < rout.length; i++)
			out[i] = rout[i];
		return out;
	}

	/**
	 * 实现交叉互换，随机出两个不相同随机数，然后交换那两个位置的点
	 * @param rout
	 * @return 输出经过交换的新路径
	 */
	public int[] swap(int[] rout) {
		Random random = new Random();
		int r1 = random.nextInt(rout.length);
		int r2 = random.nextInt(rout.length);
		while (r1==r2) {
			r2 = random.nextInt(rout.length);
		}
		int[] change = copyRout(rout);
		int tmp = change[r1];
		change[r1] = change[r2];
		change[r2] = tmp;
		return change;
	}

	/**
	 * 模拟退火算法SA
	 * @param rout	输入用于迭代的路径
	 * @param T0	初始温度
	 * @param d	温度衰减率，0.98
	 * @param Tk	最低温度 
	 * @param L		内循环次数
	 * @return	输出得到的最优路径
	 */
	public int[] Sa_TSP(int[] rout, double T0, double d, double Tk,int L) {
		// T0=1e5,d =1-7e-3, Tk=1e-3
		int[] solution = new int[rout.length];
		int[] bestpath, curentpath;
		double t = T0;
		bestpath = curentpath = copyRout(rout);
		Random random = new Random();
		while (t > Tk) {
			int it=0;
			while (it<L) {
				int[] update_path = swap(curentpath);
				int delta = cost(update_path) - cost(curentpath);
				if (delta < 0) {//为负值，即结果成本降低了，则接受
					curentpath = update_path;
					bestpath = update_path;
				} else {
					double p = Math.exp(-delta/t);
					if (random.nextDouble() <= p) {
						curentpath = update_path;
					}
				}
				it++;
			}
			t *=d;
		}
		return bestpath;
	}
	
	public void print(int rout[]) {
		System.out.println("总长度：" + cost(rout));
		System.out.print("总路线：" + rout[0]);
		for (int i = 1; i < rout.length; i++) {
			System.out.print("->" + rout[i]);
		}
		System.out.println("->0");
	}
	
	public static void main(String[] args) throws IOException {
		TspProblem problem = TspReader.readTSP("resources/eil51.txt", 51);
		SA sa = new SA(problem);
		int[] rout = sa.BFS();
		double T0=1e6;
		double d=0.99;
		double Tk=1e-6;
		int L = 20*rout.length;//内循环次数
		int[] rout2 = sa.Sa_TSP(rout, T0, d, Tk, L);
		sa.print(rout2);
	}

}
