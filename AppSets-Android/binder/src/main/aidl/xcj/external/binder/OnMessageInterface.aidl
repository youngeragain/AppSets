// OnMessageInterface.aidl
package xcj.external.binder;

// Declare any non-default types here with import statements

interface OnMessageInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

    void showNumber(int number);
    void showMessage(String message);
    String getMessage();
}