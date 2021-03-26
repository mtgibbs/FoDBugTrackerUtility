/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates, a Micro Focus company
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.bugtracker.tgt.ado.connection;

import com.fortify.bugtracker.tgt.ado.cli.ICLIOptionsADO;
import com.fortify.processrunner.cli.CLIOptionDefinitions;
import com.fortify.processrunner.context.Context;
import com.fortify.processrunner.util.rest.CLIOptionAwareProxyConfiguration;
import com.fortify.util.rest.connection.ProxyConfig;

public final class ADOConnectionFactory 
{
	public static final void addCLIOptionDefinitions(CLIOptionDefinitions cliOptionDefinitions) {
		cliOptionDefinitions.add(ICLIOptionsADO.CLI_ADO_BASE_URL);
		cliOptionDefinitions.add(ICLIOptionsADO.CLI_ADO_PAT);
		CLIOptionAwareProxyConfiguration.addCLIOptionDefinitions(cliOptionDefinitions, "ADO");
	}
	
	public static final ADORestConnection getConnection(Context context) {
		IContextADOConnection ctx = context.as(IContextADOConnection.class);
		ADORestConnection result = ctx.getADOConnection();
		if ( result == null ) {
			result = createConnection(context);
			ctx.setADOConnection(result);
		}
		return result;
	}

	private static final ADORestConnection createConnection(Context context) {
		ProxyConfig proxy = CLIOptionAwareProxyConfiguration.getProxyConfiguration(context, "ADO");
		return ADORestConnection.builder()
			.proxy(proxy)
			.baseUrl(ICLIOptionsADO.CLI_ADO_BASE_URL.getValue(context))
			.pat(ICLIOptionsADO.CLI_ADO_PAT.getValue(context))
			.build();
	}
	
	private interface IContextADOConnection {
		public void setADOConnection(ADORestConnection connection);
		public ADORestConnection getADOConnection();
	}
}
