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
 * Liefert verschiedene Werte in Abh�ngigkeit davon, ob bestimmte
 * andere Werte echt gr��er 0 sind.<br><br>
 * Aufruf: Wenn(bedingung1,ergebnis1,bedingung2,ergebnis2,...,ergebnisSonst)<br>
 * Ist bedingung1&gt;0 so wird ergebnis1 geliefert.<br>
 * Ist bedingung2&gt;0 so wird ergebnis2 geliefert.<br>
 * Sonst wird ergebnisSonst geliefert.
 * @author Alexander Herzog
 */
public class CalcSymbolPreOperatorIf extends CalcSymbolPreOperator {
	@Override
	public String[] getNames() {
		return new String[]{"Wenn","If"};
	}

	@Override
	protected Double calc(double[] parameters) {
		if (parameters.length%2!=1) return null;

		int index=0;
		while (index<parameters.length-1) {
			if (parameters[index]>0) return fastBoxedValue(parameters[index+1]);
			index+=2;
		}

		return fastBoxedValue(parameters[parameters.length-1]);
	}
}
