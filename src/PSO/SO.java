package PSO;


public class SO {
	private int x;
	private int y;
	
	public SO(int x,int y){
		this.x=x;
		this.y=y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
	public void print(){
		System.out.println("x:"+this.x+" y:"+this.y);
		
	}
	
}
