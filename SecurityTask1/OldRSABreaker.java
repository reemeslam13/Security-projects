package t2;

import java.math.BigInteger;
import java.util.ArrayList;

public class OldRSABreaker {

	private int n;
	private BigInteger e;
	private int encryptedMessage;


	public void setN(int n) {
		this.n=n;
	}

	public void setE(BigInteger e) {
		this.e=e;
	}

	public BigInteger getPhi() {
		//TO-DO
		Pair p=this.sieve();
		int newP=p.getP()-1;
		int newQ=p.getQ()-1;
		return BigInteger.valueOf(newP*newQ);
	}
	public void setEncryptedMessage(int encryptedMessage) {
		this.encryptedMessage = encryptedMessage;
	}

	public static BigInteger[] gcd(BigInteger p, BigInteger q) {
		if (q.intValue() == 0)
			return new BigInteger[] { p, BigInteger.valueOf(1),BigInteger.valueOf(0) };
		BigInteger[] vals = gcd(q,BigInteger.valueOf(p.intValue() % q.intValue()));
		BigInteger d = vals[0];
		BigInteger a = vals[2];
		BigInteger b =BigInteger.valueOf(vals[1].intValue() - (p.intValue() / q.intValue()) * vals[2].intValue());
		return new BigInteger[] { d, a, b };
	}


	public BigInteger getD() { 
		return gcd(this.e,getPhi())[1];
	}

	public BigInteger decryptMessage() {
		return BigInteger.valueOf(encryptedMessage).modPow(getD(),BigInteger.valueOf(this.n));
	}

	public Pair sieve() {
		boolean []a=new boolean[n+1];
		ArrayList<Integer>b=new ArrayList<Integer>();
		a[0]=true;
		a[1]=true;	
		for(int i=2;i<Math.sqrt(this.n);i++) {
			if(!a[i]) {
				for(int j=2;j*i<n;j++)
					a[j*i]=true;
			}
		}
		for(int i=0;i<a.length;i++)
			if(!a[i])
				b.add(i);
	    for(int i=0;i<b.size();i++) {
	    	for(int j=i+1;j<b.size();j++) {
	    		if((b.get(i)*b.get(j))== this.n)
	    		   return new Pair(b.get(i),b.get(j));
	    		
	    	}
	    }

		return null;

	}


}