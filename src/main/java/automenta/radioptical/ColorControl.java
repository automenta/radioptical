package automenta.radioptical;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.configuration.ConfigurationException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * color control that doesnt suck ( like idiotvidia's gui )
 * +
 * automatic color space learner which includes context:
 *      time of day
 *      weather
 *      current user
 *      ...etc
 *
 * tired of doing your job for you
 *
 * https://www.youtube.com/watch?v=IVpOyKCNZYw
 *
 * nvidia-settings -a ":0[DPY:DVI-I-1]/RedBrightness=0.003322"
 */
public class ColorControl extends Application {

    static ExecutorService exe = Executors.newSingleThreadExecutor();

    public static class ColorState {
        public float bri;
        public float cts;
        public float gam;
        public long time;
        public String tag;

        public ColorState() {
            tag = "";
            time = System.currentTimeMillis();
        }
    }

    public static class ColorController extends BorderPane {

        final NSlider brightSli = new NSlider(300,50).set(0, -1f, 1f);
        final NSlider contrastSli = new NSlider(300,50).set(0, -1f, 1f);
        final NSlider gammaSli = new NSlider(300,50).set(0.5, 0.1, 10f);

        final Button learn = new Button("Learn");
        final Button undo = new Button("Undo");
        final ToggleButton auto = new ToggleButton("Auto");
        ColorState state;


        public ColorController() throws ConfigurationException {
            super();

//            PropertiesConfiguration config = new PropertiesConfiguration("radioptical");
//            state = config.getProperty("state");
//            config.save();

            VBox sliders = new VBox(
                    1,
                    brightSli, contrastSli, gammaSli
            );

            HBox buttons = new HBox(
                    1,
                    learn, undo, auto
            );
            setCenter(sliders);
            setBottom(buttons);


            brightSli.value.addListener((s,p,v) -> {
                state.bri = v.floatValue();
                idiotvidia_execs();
            });
            contrastSli.value.addListener((s,p,v) -> {
                state.cts = v.floatValue();
                idiotvidia_execs();
            });
            gammaSli.value.addListener((s,p,v) -> {
                state.gam = v.floatValue();
                idiotvidia_execs();
            });

            learn.setOnAction((e) -> {
                System.out.println(state.bri + " " + state.cts + " " + state.gam);
            });
            //setMaxSize(MAX_VALUE, MAX_VALUE);

            layout();
        }

        private void idiotvidia_execs() {

            String c = "nvidia-settings ";
            c += " -a :0[DPY:DVI-I-1]/Brightness=" + state.bri;
            c += " -a :0[DPY:DVI-I-1]/Contrast=" + state.cts;
            c += " -a :0[DPY:DVI-I-1]/Gamma=" + state.gam;

            final String cmd = c;

            //run in new thread
            exe.shutdownNow();
            exe = Executors.newFixedThreadPool(2);
            exe.submit(() -> {
                try {
                    //System.out.println(cmd);

                    Process p = Runtime.getRuntime().exec(cmd);

                    /*BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(p.getInputStream()));

                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(p.getErrorStream()));

                    // read the output from the command
                    String s;
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println("  " + s);
                    }

                    // read any errors from the attempted command
                    while ((s = stdError.readLine()) != null) {
                        System.out.println("  " + s);
                    }*/

                    p.waitFor();
                    p.destroy();

                } catch (InterruptedException e) {
                    //return
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });


        }
    }

    @Override
    public void start(Stage stage) throws Exception {

        stage.setScene(new Scene(
                new ColorController()
        ));
        stage.setTitle("IdiotVidia");
        stage.show();

    }

    public static void main(String[] args) {
        ColorControl.launch();
    }
}
