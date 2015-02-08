package org.jailbreak.service.config;

public class EnvironmentFactory {
	
	private final int DEFAULT_LIMIT = 10;
	private final int MAX_LIMIT = 20;
	
    public int getDefaultLimit() {
    	String envValue = System.getenv("DEFAULT_LIMIT");
    	if (envValue == null)
    		return DEFAULT_LIMIT;
    	else
    		return Integer.parseInt(envValue);
    }
    
    public int getMaxLimit() {
    	String envValue = System.getenv("MAX_LIMIT");
    	if (envValue == null)
    		return MAX_LIMIT;
    	else
    		return Integer.parseInt(envValue);
    }
    
    public String getStripeSecretKey() {
    	String envValue = System.getenv("STRIPE_SECRET_KEY");
    	if (envValue == null) {
    		throw new RuntimeException("STRIPE_SECRET_KEY environment variable is not set. It is a requried environment varialbe.");
    	}
    	return envValue;
    }
    
    public String getDonationsWebhookSecret() {
    	String envValue = System.getenv("DONATIONS_WEBHOOK_SECRET");
    	if (envValue == null) {
    		throw new RuntimeException("DONATIONS_WEBHOOK_SECRET environment variable is not set. It is a required environment variable.");
    	}
    	return envValue;
    }

	public void requestAllManadtory() {
		// this method will trigger runtime exceptions if the ENV variables are empty
		// it is called at start-up time so that we discover these problems early
		this.getStripeSecretKey();
		this.getDonationsWebhookSecret();
	}

}
