import net.nanisl.zabuton.tool.RunnableJarBuilder;

public class Builder extends RunnableJarBuilder {

	public static void main(String[] args) {
		new Builder("zabuton").build("C:\\Program Files\\Java\\open_jdk-11");
	}

	public Builder(String appName) {
		super(appName);
	}

}
