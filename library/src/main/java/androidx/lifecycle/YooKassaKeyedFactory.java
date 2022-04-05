/*
 * The MIT License (MIT)
 * Copyright © 2022 NBCO YooMoney LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the “Software”), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package androidx.lifecycle;


import androidx.annotation.NonNull;

/**
 * Implementations of {@code Factory} interface are responsible to instantiate ViewModels.
 * <p>
 * This is more advanced version of {@link ViewModelProvider.Factory} that receives a key specified for requested
 * {@link ViewModel}.
 */
abstract class YooKassaKeyedFactory extends ViewModelProvider.OnRequeryFactory implements ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given {@code Class}.
     *
     * @param key a key associated with the requested ViewModel
     * @param modelClass a {@code Class} whose instance is requested
     * @param <T>        The type parameter for the ViewModel.
     * @return a newly created ViewModel
     */
    @NonNull
    public abstract <T extends ViewModel> T create(@NonNull String key,
                                                   @NonNull Class<T> modelClass);

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        throw new UnsupportedOperationException("create(String, Class<?>) must be called on "
                + "implementaions of KeyedFactory");
    }
}