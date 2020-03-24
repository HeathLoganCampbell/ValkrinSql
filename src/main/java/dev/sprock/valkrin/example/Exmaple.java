package dev.sprock.valkrin.example;

import dev.sprock.valkrin.Valkrin;
import dev.sprock.valkrin.tracker.ValkrinTracker;

public class Exmaple
{
    public static void main(String[] args)
    {
        Valkrin valkrin = new Valkrin("localhost", "3306", "valkrin", "root", "");

        ValkrinTracker<ExampleBase> exmapleTracker = new ValkrinTracker<>(ExampleBase.class, "Examples", valkrin);

        ExampleBase thingToSave = new ExampleBase();


        //Save the data to the database
        exmapleTracker.save("Username", thingToSave);

        //load the data from the database
        ExampleBase thingFromLoad = exmapleTracker.load("Username");

        System.out.println(thingFromLoad);

    }
}
