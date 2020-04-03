/**
 * Copyright 2020 Alexander Herzog
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package parser.symbols;

import parser.coresymbols.CalcSymbolPreOperator;

/**
 * Potenzieren
 * @author Alexander Herzog
 * @see CalcSymbolMiddleOperatorPower
 */
public final class CalcSymbolPreOperatorPower extends CalcSymbolPreOperator {

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length!=2) return null;
		if (parameters[1]<0) return null;
		return Math.pow(parameters[0],parameters[1]);
	}

	@Override
	protected double calcOrDefault(final double[] parameters, final double fallbackValue) {
		if (parameters.length!=2) return fallbackValue;
		if (parameters[1]<0) return fallbackValue;
		return Math.pow(parameters[0],parameters[1]);
	}

	@Override
	public String[] getNames() {
		return new String[]{"power","pow","potenzieren","potenz"};
	}

}
