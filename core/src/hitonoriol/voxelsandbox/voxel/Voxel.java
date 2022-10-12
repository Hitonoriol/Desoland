package hitonoriol.voxelsandbox.voxel;

public class Voxel {
	private float value;
	
	public static final Voxel air = new Voxel();
	
	public Voxel(float value) {
		this.value = value;
	}
	
	public Voxel() {
		this(Float.NaN);
	}
	
	public void setValue(float value) {
		this.value = value;
	}
	
	public float getValue() {
		return value;
	}
	
	public boolean isAir() {
		return Float.isNaN(value);
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof Voxel vox && vox.value == value;
	}
	
	@Override
	public int hashCode() {
		return Float.hashCode(value);
	}
}
