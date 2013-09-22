package sph;

public class caseMapCreator 
{
    public static void main(String[] args)
    {
    	int[] case_map = new int[256 * 9];
    	
    	int current_case = 0;
    	
    	int[] verts = {0,0,0,
    				   1,0,0,
    				   1,1,0,
    				   0,1,0,
    				   0,0,1,
    				   1,0,1,
    				   1,1,1,
    				   0,1,1};
    	
    	int[] map = {0,1,3,2,4,5,7,6};
    	
    	int[] permutation = new int[8];
    	int[] current_permutation = new int[8];
    	int[] permutation_tmp = new int[8];
    	
    	for (int t = 0; t < 256; t++) {

    		for (int i = 0; i < 8; i++) permutation[i] = i;
    		
    		int tmp = t;
    		int BITS = 0;
    		int BIT_CNT = 0;
    		int t2 = 0;
    		
    		for (int i = 0; i < 8; i++) {
    			if (tmp%2 == 1){
    				BITS++;
    			}
    			else {
    				t2 += Math.pow(2, BIT_CNT);
    			}
    						
    			tmp /= 2;
    			BIT_CNT++;
    		}
    		
    		int cube;
    		
    		if (BITS < 5) {
    			cube = t; 
    		}
    		else {
    			cube = t2;
    		}
    		
    		int newCube = 0;
    		
    		for (int i = 0; i < 4; i++) {
    		
    			newCube = 0;
    			
    			for (int l = 0; l < 8; l++) {
    				
    				int bit = cube % 2;
    				cube /= 2;
    				
    				int x = verts[3*l];
					int y = verts[3*l+2];
					int z = (verts[3*l+1] + 1) % 2;
					
					int newVert = x + 2 * y + 4 * z; 
    				
    				if (bit == 1){
    				
    					newCube += Math.pow(2,map[newVert]);
    				}
    				
    				permutation_tmp[l] = map[newVert];
    			}
    			
    			for (int l = 0; l < 8; l++) {
    				permutation[l] = permutation_tmp[permutation[l]];
    			}

    			cube = newCube;
    			
    			for (int j = 0; j < 4; j++) {
    		
    				newCube = 0;

    				for (int l = 0; l < 8; l++) {
    					
    					int bit = cube % 2;
    					cube /= 2;
    					
    					int x = (verts[3*l+2] + 1) % 2;
						int y = verts[3*l+1];
						int z = verts[3*l];
						
						int newVert = x + 2 * y + 4 * z; 
    					
    					if (bit == 1){
    						
    						newCube += Math.pow(2,map[newVert]);
    					}
    					
    					permutation_tmp[l] = map[newVert];
    				}
    				
    				for (int l = 0; l < 8; l++) {
        				permutation[l] = permutation_tmp[permutation[l]];
        			}
    				
    				cube = newCube;
    				
    				for (int k = 0; k < 4; k++) {
    		
    					newCube = 0;
    					
    					for (int l = 0; l < 8; l++) {
    						
    						int bit = cube % 2;
    						cube /= 2;
    						
    						int x = verts[3*l+1];
							int y = (verts[3*l] + 1) % 2;
							int z = verts[3*l+2];
							
							int newVert = x + 2 * y + 4 * z;
    						
    						if (bit == 1){
    							
    							newCube += Math.pow(2,map[newVert]);
    						}
    						
    						permutation_tmp[l] = map[newVert];
    					}
    					
    					for (int l = 0; l < 8; l++) {
    	    				permutation[l] = permutation_tmp[permutation[l]];
    	    			}
    					
    					cube = newCube;
    					
    					int[] cases = {0, 1, 3, 5, 65, 50, 67, 74, 51, 177, 105, 113, 58, 165, 178};

    					for (int c = 0; c < 15; c++) {
    							
							if (cube == cases[c]) {
								
								current_case = c;
								
								if (BITS > 4) {
									if (c == 3) current_case = 15;
									if (c == 6) current_case = 16;
									if (c == 7) current_case = 17;
								}
								
								for (int l = 0; l < 8; l++) {
									current_permutation[l] = permutation[l];	
								}
							}
    					}
    				}
    			}
    		}

    		case_map[9 * t] = current_case;
    		
    		// compute inverse permutation
    		for (int l = 0; l < 8; l++) {
    		for (int s = 0; s < 8; s++) {
    			if (current_permutation[s] == l) {
    				case_map[9 * t + l + 1] = s;
    			}
    		}
    		}
    	}
    	
    	System.out.print("{");
    	for (int i = 0; i < case_map.length; i++){
    		System.out.print(case_map[i]);
    		if (i < 256 * 9 - 1) {
    			System.out.print(", ");
    			if (i % 9 == 8) System.out.println();
    		}
    	}
    	System.out.print("}");
    }
}
