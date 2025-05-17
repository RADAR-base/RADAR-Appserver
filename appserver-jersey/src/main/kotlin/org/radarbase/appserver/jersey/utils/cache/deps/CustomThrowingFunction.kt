package org.radarbase.appserver.jersey.utils.cache.deps

/**
 * Functional interface that represents a function which can throw checked exceptions.
 *
 * This can be used to define lambda expressions or method references that throw exceptions
 * and handle them explicitly where required.
 *
 * @param T the input type to the function
 * @param R the result type of the function
 */
fun interface CustomThrowingFunction<in T, out R> {
    /**
     * Applies the given transformation logic on the input of type T and returns a result of type R.
     *
     * @param t The input parameter of type T on which the operation is to be applied.
     * @return The result of the operation as an instance of type R.
     * @throws Exception If an exception occurs during the application of the transformation.
     */
    @Throws(Exception::class)
    suspend fun applyWithException(key: T): R
}
