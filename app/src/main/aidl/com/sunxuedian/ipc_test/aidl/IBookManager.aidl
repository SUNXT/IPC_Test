// IBookManager.aidl
package com.sunxuedian.ipc_test.aidl;

// Declare any non-default types here with import statements
import com.sunxuedian.ipc_test.aidl.Book;

interface IBookManager {

    List<Book> getBookList();
    void addBook(in Book book);
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

}
