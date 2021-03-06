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
package parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import parser.coresymbols.CalcSymbol;
import parser.coresymbols.CalcSymbolConst;
import parser.coresymbols.CalcSymbolMiddleOperator;
import parser.coresymbols.CalcSymbolNumber;
import parser.coresymbols.CalcSymbolPostOperator;
import parser.coresymbols.CalcSymbolPreOperator;
import parser.coresymbols.CalcSymbolVariable;
import parser.symbols.CalcSymbolConstE;
import parser.symbols.CalcSymbolConstPi;
import parser.symbols.CalcSymbolDiscreteDistributionBinomial;
import parser.symbols.CalcSymbolDiscreteDistributionHyperGeom;
import parser.symbols.CalcSymbolDiscreteDistributionPoisson;
import parser.symbols.CalcSymbolDistributionBeta;
import parser.symbols.CalcSymbolDistributionCauchy;
import parser.symbols.CalcSymbolDistributionChi;
import parser.symbols.CalcSymbolDistributionChiSquare;
import parser.symbols.CalcSymbolDistributionExp;
import parser.symbols.CalcSymbolDistributionF;
import parser.symbols.CalcSymbolDistributionFatigueLife;
import parser.symbols.CalcSymbolDistributionFrechet;
import parser.symbols.CalcSymbolDistributionGamma;
import parser.symbols.CalcSymbolDistributionGumbel;
import parser.symbols.CalcSymbolDistributionHyperbolicSecant;
import parser.symbols.CalcSymbolDistributionInverseGaussian;
import parser.symbols.CalcSymbolDistributionJohnsonSU;
import parser.symbols.CalcSymbolDistributionLaplace;
import parser.symbols.CalcSymbolDistributionLogLogistic;
import parser.symbols.CalcSymbolDistributionLogNormal;
import parser.symbols.CalcSymbolDistributionLogistic;
import parser.symbols.CalcSymbolDistributionNormal;
import parser.symbols.CalcSymbolDistributionPareto;
import parser.symbols.CalcSymbolDistributionPert;
import parser.symbols.CalcSymbolDistributionPower;
import parser.symbols.CalcSymbolDistributionRayleigh;
import parser.symbols.CalcSymbolDistributionTriangular;
import parser.symbols.CalcSymbolDistributionUniform;
import parser.symbols.CalcSymbolDistributionWeibull;
import parser.symbols.CalcSymbolEmpiricalDistributionCDF;
import parser.symbols.CalcSymbolEmpiricalDistributionCV;
import parser.symbols.CalcSymbolEmpiricalDistributionMean;
import parser.symbols.CalcSymbolEmpiricalDistributionMedian;
import parser.symbols.CalcSymbolEmpiricalDistributionPDF;
import parser.symbols.CalcSymbolEmpiricalDistributionQuantil;
import parser.symbols.CalcSymbolEmpiricalDistributionRandom;
import parser.symbols.CalcSymbolEmpiricalDistributionSD;
import parser.symbols.CalcSymbolEmpiricalDistributionVar;
import parser.symbols.CalcSymbolMiddleOperatorDivide;
import parser.symbols.CalcSymbolMiddleOperatorMinus;
import parser.symbols.CalcSymbolMiddleOperatorMultiply;
import parser.symbols.CalcSymbolMiddleOperatorPlus;
import parser.symbols.CalcSymbolMiddleOperatorPower;
import parser.symbols.CalcSymbolPostOperatorDEGtoRAD;
import parser.symbols.CalcSymbolPostOperatorFactorial;
import parser.symbols.CalcSymbolPostOperatorPercent;
import parser.symbols.CalcSymbolPostOperatorPower2;
import parser.symbols.CalcSymbolPostOperatorPower3;
import parser.symbols.CalcSymbolPreOperatorAbs;
import parser.symbols.CalcSymbolPreOperatorAllenCunneen;
import parser.symbols.CalcSymbolPreOperatorArcCos;
import parser.symbols.CalcSymbolPreOperatorArcCosh;
import parser.symbols.CalcSymbolPreOperatorArcCot;
import parser.symbols.CalcSymbolPreOperatorArcCoth;
import parser.symbols.CalcSymbolPreOperatorArcSin;
import parser.symbols.CalcSymbolPreOperatorArcSinh;
import parser.symbols.CalcSymbolPreOperatorArcTan;
import parser.symbols.CalcSymbolPreOperatorArcTanh;
import parser.symbols.CalcSymbolPreOperatorBinomial;
import parser.symbols.CalcSymbolPreOperatorCV;
import parser.symbols.CalcSymbolPreOperatorCeil;
import parser.symbols.CalcSymbolPreOperatorCos;
import parser.symbols.CalcSymbolPreOperatorCosh;
import parser.symbols.CalcSymbolPreOperatorCot;
import parser.symbols.CalcSymbolPreOperatorCoth;
import parser.symbols.CalcSymbolPreOperatorErlangC;
import parser.symbols.CalcSymbolPreOperatorExp;
import parser.symbols.CalcSymbolPreOperatorFactorial;
import parser.symbols.CalcSymbolPreOperatorFloor;
import parser.symbols.CalcSymbolPreOperatorFrac;
import parser.symbols.CalcSymbolPreOperatorGamma;
import parser.symbols.CalcSymbolPreOperatorIf;
import parser.symbols.CalcSymbolPreOperatorInt;
import parser.symbols.CalcSymbolPreOperatorLd;
import parser.symbols.CalcSymbolPreOperatorLg;
import parser.symbols.CalcSymbolPreOperatorLog;
import parser.symbols.CalcSymbolPreOperatorMax;
import parser.symbols.CalcSymbolPreOperatorMean;
import parser.symbols.CalcSymbolPreOperatorMedian;
import parser.symbols.CalcSymbolPreOperatorMin;
import parser.symbols.CalcSymbolPreOperatorModulo;
import parser.symbols.CalcSymbolPreOperatorPower;
import parser.symbols.CalcSymbolPreOperatorRandom;
import parser.symbols.CalcSymbolPreOperatorRound;
import parser.symbols.CalcSymbolPreOperatorSCV;
import parser.symbols.CalcSymbolPreOperatorSign;
import parser.symbols.CalcSymbolPreOperatorSin;
import parser.symbols.CalcSymbolPreOperatorSinh;
import parser.symbols.CalcSymbolPreOperatorSqr;
import parser.symbols.CalcSymbolPreOperatorSqrt;
import parser.symbols.CalcSymbolPreOperatorStdDev;
import parser.symbols.CalcSymbolPreOperatorSum;
import parser.symbols.CalcSymbolPreOperatorTan;
import parser.symbols.CalcSymbolPreOperatorTanh;
import parser.symbols.CalcSymbolPreOperatorVariance;

/**
 * Diese Klasse h�lt alle Symbole, die im Formelparser zur Verf�gung stehen sollen, vor.
 * @author Alexander Herzog
 * @see CalcParser#CalcParser(CalcSymbolList)
 * @version 1.1
 */
public class CalcSymbolList {
	private final String[] variables;

	private static final List<CalcSymbolConst> listConst=new ArrayList<>();
	private static final List<CalcSymbolPostOperator> listPostOperator=new ArrayList<>();
	private static final List<CalcSymbolMiddleOperator> listMiddleOperator=new ArrayList<>();
	private static final List<CalcSymbolPreOperator> listPreOperator=new ArrayList<>();
	private static final List<String> listNames=new ArrayList<>();
	private static final List<String> listNamesLower=new ArrayList<>();
	private static final Semaphore initLock=new Semaphore(1);
	private List<CalcSymbolPreOperator> listPreOperatorUser=null;
	private String[] allSymbolNames=null;
	private String[] allSymbolNamesLower=null;

	private static final Object initSync=new Object();

	/**
	 * Konstruktor der Klasse <code>CalcSymbolList</code>
	 * @param variables	Variablen, die zus�tzlich zu den normalen Funktionen zur Verf�gung stehen sollen
	 */
	public CalcSymbolList(final String[] variables) {
		this.variables=(variables==null)?new String[0]:variables;
		synchronized(initSync) {
			if (listConst.isEmpty()) {
				initSymbols();
				for (int i=0;i<listPreOperator.size();i++) listNames.addAll(Arrays.asList(listPreOperator.get(i).getNames()));
				for (int i=0;i<listMiddleOperator.size();i++) listNames.addAll(Arrays.asList(listMiddleOperator.get(i).getNames()));
				for (int i=0;i<listPostOperator.size();i++) listNames.addAll(Arrays.asList(listPostOperator.get(i).getNames()));
				for (int i=0;i<listConst.size();i++) listNames.addAll(Arrays.asList(listConst.get(i).getNames()));
				for (String name: listNames) listNamesLower.add(name.toLowerCase());
			}
		}
	}

	/**
	 * Liefert eine Liste mit zus�tzlichen, nicht bereits in {@link CalcSymbolList} enthaltenen Symbolen
	 * @return	Liste mit zus�tzlichen Symbolen
	 */
	protected List<CalcSymbolPreOperator> getUserFunctions() {
		return null;
	}

	private static void addSymbol(final CalcSymbol symbol) {
		switch (symbol.getType()) {
		case TYPE_CONST : listConst.add((CalcSymbolConst)symbol); break;
		case TYPE_FUNCTION : listPreOperator.add((CalcSymbolPreOperator)symbol); break;
		case TYPE_MIDDLE_OPERATOR: listMiddleOperator.add((CalcSymbolMiddleOperator)symbol); break;
		case TYPE_POST_OPERATOR: listPostOperator.add((CalcSymbolPostOperator)symbol); break;
		case TYPE_SUB: /* Gibt's nicht in einer Liste. */ break;
		}
	}

	private static void initSymbols() {
		initLock.acquireUninterruptibly();
		try {
			if (!listConst.isEmpty()) return;

			addSymbol(new CalcSymbolDiscreteDistributionHyperGeom());
			addSymbol(new CalcSymbolDiscreteDistributionBinomial());
			addSymbol(new CalcSymbolDiscreteDistributionPoisson());

			addSymbol(new CalcSymbolDistributionExp());
			addSymbol(new CalcSymbolDistributionUniform());
			addSymbol(new CalcSymbolDistributionNormal());
			addSymbol(new CalcSymbolDistributionLogNormal());
			addSymbol(new CalcSymbolDistributionGamma());
			addSymbol(new CalcSymbolDistributionBeta());
			addSymbol(new CalcSymbolDistributionWeibull());
			addSymbol(new CalcSymbolDistributionCauchy());
			addSymbol(new CalcSymbolDistributionChiSquare());
			addSymbol(new CalcSymbolDistributionChi());
			addSymbol(new CalcSymbolDistributionF());
			addSymbol(new CalcSymbolDistributionJohnsonSU());
			addSymbol(new CalcSymbolDistributionTriangular());
			addSymbol(new CalcSymbolDistributionPert());
			addSymbol(new CalcSymbolDistributionLaplace());
			addSymbol(new CalcSymbolDistributionPareto());
			addSymbol(new CalcSymbolDistributionLogistic());
			addSymbol(new CalcSymbolDistributionInverseGaussian());
			addSymbol(new CalcSymbolDistributionRayleigh());
			addSymbol(new CalcSymbolDistributionLogLogistic());
			addSymbol(new CalcSymbolDistributionPower());
			addSymbol(new CalcSymbolDistributionGumbel());
			addSymbol(new CalcSymbolDistributionFatigueLife());
			addSymbol(new CalcSymbolDistributionFrechet());
			addSymbol(new CalcSymbolDistributionHyperbolicSecant());
			addSymbol(new CalcSymbolEmpiricalDistributionPDF());
			addSymbol(new CalcSymbolEmpiricalDistributionCDF());
			addSymbol(new CalcSymbolEmpiricalDistributionRandom());
			addSymbol(new CalcSymbolEmpiricalDistributionMean());
			addSymbol(new CalcSymbolEmpiricalDistributionMedian());
			addSymbol(new CalcSymbolEmpiricalDistributionQuantil());
			addSymbol(new CalcSymbolEmpiricalDistributionSD());
			addSymbol(new CalcSymbolEmpiricalDistributionVar());
			addSymbol(new CalcSymbolEmpiricalDistributionCV());

			addSymbol(new CalcSymbolPreOperatorSqrt());
			addSymbol(new CalcSymbolPreOperatorSqr());
			addSymbol(new CalcSymbolPreOperatorPower());
			addSymbol(new CalcSymbolPreOperatorSinh());
			addSymbol(new CalcSymbolPreOperatorCosh());
			addSymbol(new CalcSymbolPreOperatorTanh());
			addSymbol(new CalcSymbolPreOperatorCoth());
			addSymbol(new CalcSymbolPreOperatorSin());
			addSymbol(new CalcSymbolPreOperatorCos());
			addSymbol(new CalcSymbolPreOperatorTan());
			addSymbol(new CalcSymbolPreOperatorCot());
			addSymbol(new CalcSymbolPreOperatorArcSin());
			addSymbol(new CalcSymbolPreOperatorArcCos());
			addSymbol(new CalcSymbolPreOperatorArcTan());
			addSymbol(new CalcSymbolPreOperatorArcCot());
			addSymbol(new CalcSymbolPreOperatorArcSinh());
			addSymbol(new CalcSymbolPreOperatorArcCosh());
			addSymbol(new CalcSymbolPreOperatorArcTanh());
			addSymbol(new CalcSymbolPreOperatorArcCoth());
			addSymbol(new CalcSymbolPreOperatorExp());
			addSymbol(new CalcSymbolPreOperatorLog());
			addSymbol(new CalcSymbolPreOperatorLg());
			addSymbol(new CalcSymbolPreOperatorLd());
			addSymbol(new CalcSymbolPreOperatorGamma());
			addSymbol(new CalcSymbolPreOperatorAbs());
			addSymbol(new CalcSymbolPreOperatorMin());
			addSymbol(new CalcSymbolPreOperatorMax());
			addSymbol(new CalcSymbolPreOperatorFrac());
			addSymbol(new CalcSymbolPreOperatorInt());
			addSymbol(new CalcSymbolPreOperatorSign());
			addSymbol(new CalcSymbolPreOperatorRound());
			addSymbol(new CalcSymbolPreOperatorFloor());
			addSymbol(new CalcSymbolPreOperatorCeil());
			addSymbol(new CalcSymbolPreOperatorFactorial());
			addSymbol(new CalcSymbolPreOperatorBinomial());
			addSymbol(new CalcSymbolPreOperatorRandom());
			addSymbol(new CalcSymbolPreOperatorSum());
			addSymbol(new CalcSymbolPreOperatorMean());
			addSymbol(new CalcSymbolPreOperatorMedian());
			addSymbol(new CalcSymbolPreOperatorVariance());
			addSymbol(new CalcSymbolPreOperatorStdDev());
			addSymbol(new CalcSymbolPreOperatorSCV());
			addSymbol(new CalcSymbolPreOperatorCV());
			addSymbol(new CalcSymbolPreOperatorErlangC());
			addSymbol(new CalcSymbolPreOperatorAllenCunneen());
			addSymbol(new CalcSymbolPreOperatorModulo());
			addSymbol(new CalcSymbolPreOperatorIf());

			addSymbol(new CalcSymbolMiddleOperatorPlus());
			addSymbol(new CalcSymbolMiddleOperatorMinus());
			addSymbol(new CalcSymbolMiddleOperatorMultiply());
			addSymbol(new CalcSymbolMiddleOperatorDivide());
			addSymbol(new CalcSymbolMiddleOperatorPower());

			addSymbol(new CalcSymbolPostOperatorPercent());
			addSymbol(new CalcSymbolPostOperatorDEGtoRAD());
			addSymbol(new CalcSymbolPostOperatorPower2());
			addSymbol(new CalcSymbolPostOperatorPower3());
			addSymbol(new CalcSymbolPostOperatorFactorial());

			addSymbol(new CalcSymbolConstE());
			addSymbol(new CalcSymbolConstPi());
		} finally {
			initLock.release();
		}
	}

	/**
	 * Liefert eine Liste aller dem System bekannten Symbole (einschlie�lich Variablennamen)
	 * @return	Liste aller bekannten Symbole
	 */
	public String[] getAllSymbolNames() {
		if (allSymbolNames==null) {
			if (listPreOperatorUser==null) listPreOperatorUser=getUserFunctions();

			int size=0;
			if (listPreOperatorUser!=null) size+=listPreOperatorUser.size()*3;
			size+=listNames.size();
			size+=variables.length;

			final List<String> allSymbolNamesList=new ArrayList<>(size);
			if (listPreOperatorUser!=null) for (int i=0;i<listPreOperatorUser.size();i++) allSymbolNamesList.addAll(Arrays.asList(listPreOperatorUser.get(i).getNames()));
			allSymbolNamesList.addAll(listNames);
			for (final String variable: variables) if (variable!=null) allSymbolNamesList.add(variable);

			allSymbolNames=allSymbolNamesList.toArray(new String[0]);
		}

		return allSymbolNames;
	}

	private static final String[] emptyArray=new String[0];

	/**
	 * Liefert eine Liste aller dem System bekannten Symbole (einschlie�lich Variablennamen) in Kleinschreibweise
	 * @return	Liste aller bekannten Symbole
	 */
	public String[] getAllSymbolNamesLower() {
		if (allSymbolNamesLower==null) {
			if (listPreOperatorUser==null) listPreOperatorUser=getUserFunctions();

			int size=0;
			if (listPreOperatorUser!=null) size+=listPreOperatorUser.size()*3;
			size+=listNames.size();
			size+=variables.length;

			final List<String> allSymbolNamesList=new ArrayList<>(size);
			if (listPreOperatorUser!=null) for (int i=0;i<listPreOperatorUser.size();i++) allSymbolNamesList.addAll(Arrays.asList(listPreOperatorUser.get(i).getNames()));
			allSymbolNamesList.addAll(listNamesLower);
			for (final String variable: variables) if (variable!=null) {
				final String s=variable.trim();
				if (!s.isEmpty()) allSymbolNamesList.add(s);
			}

			allSymbolNamesLower=allSymbolNamesList.toArray(emptyArray);

			final int count=allSymbolNamesLower.length;
			for (int i=0;i<count;i++) {
				boolean needsProcessing=false;
				final int len=allSymbolNamesLower[i].length();
				for (int j=0;j<len;j++) {
					final char c=allSymbolNamesLower[i].charAt(j);
					if (c>='A' && c<='Z') {needsProcessing=true; break;}
				}
				if (needsProcessing) allSymbolNamesLower[i]=allSymbolNamesLower[i].toLowerCase();
			}
		}
		return allSymbolNamesLower;
	}

	/**
	 * Sucht ein Symbol mit einem bestimmten Namen
	 * @param name	Name zu dem ein passendes Symbol gesucht werden soll
	 * @return	Symbol mit dem angegebenen Namen oder <code>null</code> wenn kein Symbol mit diesem Namen gefunden wurde
	 */
	public CalcSymbol findSymbol(final String name) {
		if (name==null || name.trim().isEmpty()) return null;

		CalcSymbol select=null;
		int len=0;
		if (listPreOperatorUser==null) listPreOperatorUser=getUserFunctions();

		if (listPreOperatorUser!=null) for (CalcSymbol sym: listPreOperatorUser) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}
		for (CalcSymbol sym: listPreOperator) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}
		for (CalcSymbol sym: listMiddleOperator) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}
		for (CalcSymbol sym: listPostOperator) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}
		for (CalcSymbol sym: listConst) for (String s: sym.getNames()) if (!s.isEmpty() && s.equalsIgnoreCase(name) && s.length()>len) {select=sym; len=s.length();}

		for (int i=0;i<variables.length;i++) if (variables[i]!=null && variables[i].equalsIgnoreCase(name) && variables[i].length()>len) {
			CalcSymbolVariable variable=new CalcSymbolVariable();
			variable.setData(i);
			select=variable;
			len=variables[i].length();
		}

		if (select!=null) select=select.cloneSymbol();
		return select;
	}

	/**
	 * Erzeugt ein Symbol, welches eine Zahl repr�sentiert
	 * @param d	Zahl, die in Form eines {@link CalcSymbol}-Objekts ausgedr�ckt werden soll
	 * @return	Objekt zur Verwendung in Formel-Baumstrukturen
	 */
	public CalcSymbolNumber getNumber(final double d) {
		CalcSymbolNumber sym=new CalcSymbolNumber();
		sym.setValue(d);
		return sym;
	}
}