package org.simpleds.testdb;

/**
 *
 */
public class FoobarPlugin extends Plugin {

    public int id;

    private FoobarPlugin() {
    }

    public FoobarPlugin(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
