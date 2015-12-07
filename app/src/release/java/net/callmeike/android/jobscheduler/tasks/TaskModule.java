/* $Id: $
   Copyright 2012, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package net.callmeike.android.jobscheduler.tasks;

import javax.inject.Singleton;

import net.callmeike.android.jobscheduler.net.Client;

import dagger.Module;
import dagger.Provides;


/**
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 * @version $Revision: $
 */
@Module
class TaskModule {
    @Provides
    @Singleton
    SampleTask providePeriodicTask(Client client) {
        return new SampleTask(client);
    }
}
