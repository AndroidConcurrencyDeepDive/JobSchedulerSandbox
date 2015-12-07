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
package net.callmeike.android.jobscheduler.net;

import android.util.Log;


/**
 * @author <a href="mailto:blake.meike@gmail.com">G. Blake Meike</a>
 * @version $Revision: $
 */
public class Client {
    private static final String TAG = "NET";

    private final String endpoint;

    public Client(String endpoint) { this.endpoint = endpoint; }

    public void send(String req) {
        Log.i(TAG, "Sending: " + req + " @" + endpoint);
        try { Thread.sleep(30 * 1000); }
        catch (InterruptedException ignore) { }
    }
}
