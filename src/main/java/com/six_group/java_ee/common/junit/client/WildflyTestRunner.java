/**
 * junit-remote
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * @author Tradeshift - http://www.tradeshift.com
 */

package com.six_group.java_ee.common.junit.client;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import com.six_group.java_ee.common.junit.api.Utils;

public class WildflyTestRunner extends Runner implements Filterable, Sortable {
    private final boolean _isFirstTestCase;
    private final boolean _isLastTestCase;
    private Runner _delegate;

    public WildflyTestRunner(final Class<?> clazz) throws InitializationError { 
        _isFirstTestCase = true;
        _isLastTestCase = true;
        _delegate = new ClientRemoteRunner(clazz, _isFirstTestCase, _isLastTestCase);
    }
    
    public WildflyTestRunner(final Class<?> clazz, final boolean isFirstTestCase, final boolean isLastTestCase) throws InitializationError {  
        _isFirstTestCase = isFirstTestCase;
        _isLastTestCase = isLastTestCase;
        _delegate = new ClientRemoteRunner(clazz, _isFirstTestCase, _isLastTestCase);
    }

    @Override
    public Description getDescription() {
        return _delegate.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        _delegate.run(notifier);
    }

    @Override
    public void filter(Filter filter) throws NoTestsRemainException {
        Utils.filter(_delegate, filter);
    }

    @Override
    public void sort(Sorter sorter) {
        Utils.sort(_delegate, sorter);
    }
}
