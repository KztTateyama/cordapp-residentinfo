package com.template;

import net.corda.core.identity.CordaX500Name;
import net.corda.testing.core.TestIdentity;

public class TestUtils {

	public static TestIdentity ACity = new TestIdentity(new CordaX500Name("ACity", "TestLand", "US"));
	public static TestIdentity BCity = new TestIdentity(new CordaX500Name("BCity", "TestCity", "US"));
	public static TestIdentity XCity = new TestIdentity(new CordaX500Name("XCity", "TestCity", "US"));
	public static TestIdentity YCity = new TestIdentity(new CordaX500Name("YCity", "TestCity", "US"));
//	public static TestIdentity CHARLIE = new TestIdentity(new CordaX500Name("Charlie", "TestVillage", "US"));
//	public static TestIdentity MINICORP = new TestIdentity(new CordaX500Name("MiniCorp", "MiniLand", "US"));
//	public static TestIdentity MEGACORP = new TestIdentity(new CordaX500Name("MegaCorp", "MiniLand", "US"));
//	public static TestIdentity DUMMY = new TestIdentity(new CordaX500Name("Dummy", "FakeLand", "US"));
}
