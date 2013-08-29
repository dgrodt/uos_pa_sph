//#define BUFFER_SIZE_SIDE 128
//#define BUFFER_SIZE_DEPTH 32
//#define OFFSET 4
#define BUFFER_SIZE_SIDE 32
#define BUFFER_SIZE_DEPTH 64
#define OFFSET 3
#define H 0.2

float W (float4* r) {
	float x = fast_length(*r);
	float k = 315 / (64 * 3.14159 * H*H*H*H*H*H*H*H*H);
	if (x > H) return 0;
	return k * (H*H-x*x)*(H*H-x*x)*(H*H-x*x);
}

float W2 (float4* r, float h) {
	float x = fast_length(*r);
	float k = 315 / (64 * 3.14159 * h*h*h*h*h*h*h*h*h);
	if (x > h) return 0;
	return k * (h*h-x*x)*(h*h-x*x)*(h*h-x*x);
}
float4 gradW (float4* r) {
	float x = fast_length(*r);
	float k = 45 / (3.14159 * H*H*H*H*H*H);
	if (x > H || x == 0) return (float4)0;
	return k * ((H-x)*(H-x)*(H-x)) * (*r) / x;
}
float4 gradWV (float4* r) {
	float x = fast_length(*r);
	float k = 45 / (3.14159 * H*H*H*H*H*H);
	if (x > H || x == 0) return 0;
	return k * (H - x);
}


kernel void sph_CalcNewRho(
global float4* body_Pos,
global float* body_rho,
const float m,
global uint* data
)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	float rhoByM = 0;
	
	float4 pos = body_Pos[id];
	int4 gridPos = convert_int4((BUFFER_SIZE_SIDE - 1) * (body_Pos[id] + (float4)1) / 2);
	
	for (int l = max(gridPos.x - OFFSET, 0); l <= min(gridPos.x + OFFSET, BUFFER_SIZE_SIDE - 1) ; l++) 
	{
		for (int j = max(gridPos.y - OFFSET, 0); j <= min(gridPos.y + OFFSET, BUFFER_SIZE_SIDE - 1) ; j++) 
		{
			for (int k = max(gridPos.z - OFFSET, 0); k <= min(gridPos.z + OFFSET, BUFFER_SIZE_SIDE - 1) ; k++) 
			{
	 			int cnt_ind = BUFFER_SIZE_DEPTH * (((BUFFER_SIZE_SIDE + l)%BUFFER_SIZE_SIDE) + BUFFER_SIZE_SIDE * ((BUFFER_SIZE_SIDE + j)%BUFFER_SIZE_SIDE) + BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * ((BUFFER_SIZE_SIDE + k)%BUFFER_SIZE_SIDE));
				uint cnt = data[cnt_ind];
				for (int o = 1; o <= cnt; o++) 
				{
					int i = data[cnt_ind + o];
					if (i!=id)
					{
						float4 w = pos-body_Pos[i];
						rhoByM += W(&w);
					}
				}
			}
		}
	}
	float rho = m * rhoByM;
	if(rho == 0)
	{
		return;
	}
	body_rho[id] = rho;
}


kernel void sph_CalcNewP(
global float* body_P,
global float* body_rho,
const float rho
)
{
	uint id = get_global_id(0);
	float b_roh = body_rho[id];
	body_P[id] = (( (b_roh/rho)*(b_roh/rho)*(b_roh/rho)*(b_roh/rho)*(b_roh/rho)*(b_roh/rho)*(b_roh/rho)*(b_roh/rho) ) -1) /300;
}


kernel void sph_CalcNewN(
global float4* body_Pos,
global float* body_rho,
global float4* n,
global uint* data
)
{
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	float4 new_n = (float4)0;
	float4 pos = body_Pos[id];
	float h = 0.2;
	
	int4 gridPos = convert_int4((BUFFER_SIZE_SIDE - 1) * (body_Pos[id] + (float4)1) / 2);

	for (int l = max(gridPos.x - OFFSET+1, 0); l <= min(gridPos.x + OFFSET+1, BUFFER_SIZE_SIDE - 1) ; l++) 
	{
		for (int j = max(gridPos.y - OFFSET+1, 0); j <= min(gridPos.y + OFFSET+1, BUFFER_SIZE_SIDE - 1) ; j++) 
		{
			for (int k = max(gridPos.z - OFFSET+1, 0); k <= min(gridPos.z + OFFSET+1, BUFFER_SIZE_SIDE - 1) ; k++) 
			{
	 			int cnt_ind = BUFFER_SIZE_DEPTH * (l + BUFFER_SIZE_SIDE * j + BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * k);
				uint cnt = data[cnt_ind];
				for (int o = 1; o <= cnt; o++) 
				{
					int i = data[cnt_ind + o];	
					if (i!=id)
					{	
						float4 w = pos-body_Pos[i];
						new_n += gradW(&w) / (body_rho[i] * 300000);
					}
				}
			}
		}
	}
	//if (fast_length(new_n) > 10) {
		n[id] = new_n / fast_length(new_n);
		n[id].w = fast_length(new_n);
	//}
	//else {
		//n[id] = (float4)0;
	//}
}


kernel void sph_CalcNewV(
global float4* body_Pos,
global float4* body_V,
const float DELTA_T,
global float* body_P,
global float* body_rho,
global float4* n,
const float m,
global uint* data
)
{	
	uint id = get_global_id(0);
	uint N = get_global_size(0);
	

	//float nu = 0.000001;
	float nu = 0.0000005;

	float tau = 0;
	float4 g = (float4)(0,-10000,0,0);
	float4 a_P = (float4)0;
	float4 a_V = (float4)0;
	float4 a_W = (float4)0;
	float4 a_T = (float4)0;
	
	//---------------------------------------------------
	//		calculate pressure and viscosity forces
	//---------------------------------------------------
	
	float4 pos = body_Pos[id];
	float4 V = body_V[id];
	float P = body_P[id];
	
	int4 gridPos = convert_int4((BUFFER_SIZE_SIDE - 1) * (pos + (float4)1) / 2);
	
	for (int l = max(gridPos.x - OFFSET, 0); l <= min(gridPos.x + OFFSET, BUFFER_SIZE_SIDE - 1) ; l++) 
	{
		for (int j = max(gridPos.y - OFFSET, 0); j <= min(gridPos.y + OFFSET, BUFFER_SIZE_SIDE - 1) ; j++) 
		{
			for (int k = max(gridPos.z - OFFSET, 0); k <= min(gridPos.z + OFFSET, BUFFER_SIZE_SIDE - 1) ; k++) 
			{
	 			int cnt_ind = BUFFER_SIZE_DEPTH * (l + BUFFER_SIZE_SIDE * j + BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * k);
				uint cnt = data[cnt_ind];
				for (int o = 1; o <= cnt; o++) 
				{
					int i = data[cnt_ind + o];
					float rho = body_rho[i];
					if (id!=i) 
					{
						float4 r = pos-body_Pos[i];	
						a_V += -nu  * (V - body_V[i]) * gradWV(&r) / rho;
						float4 grad = gradW(&r);	
						float C = (body_P[i] + P)/(2 * rho);
						a_P += C * grad;
					}
				}
			}
		}
	}
	
	//---------------------------------------------------
	//		calculate boundary forces
	//---------------------------------------------------
	float4 down  = (float4)(pos.x, -1, pos.z, 0);
	float4 up    = (float4)(pos.x, 1, pos.z, 0);
	float4 back  = (float4)(pos.x, pos.y, -1, 0);
	float4 front = (float4)(pos.x, pos.y, 1, 0);
	float4 left  = (float4)(-1, pos.y, pos.z, 0);
	float4 right = (float4)(1, pos.y, pos.z, 0);
	
	float r;
	r = fast_distance(pos, down);
	if(r < 0.05){ a_W += (0.05 - r) * (pos - down) / (fast_length(pos - down) * pown(DELTA_T,2)); }
	r = fast_distance(pos, up);
	if(r < 0.05){ a_W += (0.05 - r)   * (pos - up)   / (fast_length(pos - up)   * pown(DELTA_T,2)); }
	r = fast_distance(pos, back);
	if(r < 0.05){ a_W += (0.05 - r) * (pos - back) / (fast_length(pos - back) * pown(DELTA_T,2)); }
	r = fast_distance(pos, front);
	if(r < 0.05){ a_W += (0.05 - r) * (pos - front)/ (fast_length(pos - front)* pown(DELTA_T,2)); }
	r = fast_distance(pos, left);
	if(r < 0.05){ a_W += (0.05 - r) * (pos - left) / (fast_length(pos - left) * pown(DELTA_T,2)); }
	r = fast_distance(pos, right);
	if(r < 0.05){ a_W += (0.05 - r) * (pos - right)/ (fast_length(pos - right)* pown(DELTA_T,2)); }

	//---------------------------------------------------
	//		calculate tension forces
	//---------------------------------------------------
	/*
	float nabla_n = 0;
	for (int i = 0; i < N; i++) 
	{
		float4 r = body_Pos[id]-body_Pos[i];
		if (fast_length(n[i]) > 0) 
		{
			if (i!=id)
			{
				nabla_n +=  dot(n[i]/fast_length(n[i]), gradW(r, 0.2)) / body_rho[i];
			}
		}
	}	
	a_T = tau * nabla_n * n[id];
	*/
	body_V[id] += (a_V + a_P + a_W + g + a_T) * DELTA_T;
}

kernel void sph_resetData(
global uint* data
)
{
	uint id = get_global_id(0);
	data[BUFFER_SIZE_DEPTH * id] = 0;
}


kernel void sph_CalcNewPos(
global float4* body_Pos, 
global float4* body_V,
const float DELTA_T,
global uint* data
)
{
    uint id = get_global_id(0);
   
    float4 pos = body_Pos[id] + body_V[id] * DELTA_T;
    body_Pos[id] = pos;
    
    int3 gridPos = convert_int3((BUFFER_SIZE_SIDE - 1) * (pos.xyz + (float3)1) / 2);
    int cnt_ind = BUFFER_SIZE_DEPTH * (gridPos.x + BUFFER_SIZE_SIDE * gridPos.y + BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * gridPos.z);

	if (0 <= cnt_ind && cnt_ind < BUFFER_SIZE_DEPTH * BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE) {
	    int cnt = atomic_inc(data + cnt_ind) + 1;
	    if(cnt < BUFFER_SIZE_DEPTH - 1) {
	    	data[cnt_ind + cnt] = id;
	    }
	}
}



//--------------------------------------------------------
//		KERNELS FOR SURFACE CALCULATION
//--------------------------------------------------------


kernel void sph_resetSurfaceRho(
global int* surface_grid_rho,
global int* COUNT
)
{
	uint id_x = get_global_id(0);
	uint id_y = get_global_id(1);
	uint id_z = get_global_id(2);
	uint gridSize = get_global_size(0);
	
	surface_grid_rho[id_x + gridSize * id_y + gridSize * gridSize * id_z] = 0;
	
	COUNT[0] = 0;
}


kernel void sph_CalcNewSurface(
global float4* body_Pos,
global float4* surface_Pos,
global float* body_rho,
global int* surface_grid_rho,
const float m,
const int gridSize,
global uint* data
)
{
	uint id = get_global_id(0);
	
	float grid_rho = 0;
	float h = 0.15;
	float4 pos = body_Pos[id];
	int offset = (int)(h * gridSize);
	
	int4 gridPos = convert_int4((gridSize - 1) * (pos + (float4)1) / 2);
	
	for (int l = max(gridPos.x - offset, 0); l <= min(gridPos.x + offset, gridSize - 1) ; l++) {
	for (int j = max(gridPos.y - offset, 0); j <= min(gridPos.y + offset, gridSize - 1) ; j++) {
	for (int k = max(gridPos.z - offset, 0); k <= min(gridPos.z + offset, gridSize - 1) ; k++) {

		float4 surface_gridPos = 2 * (float4)(l, j, k, 0) / gridSize - (float4)(1,1,1,0);
		float4 diff = pos - surface_gridPos;
	
		float d = dot(diff, diff);
		
		
		if (d < h * h) {
			atomic_add(surface_grid_rho + (l + gridSize * j + gridSize * gridSize * k), (int)(W2(&diff, h) * m * 10000000000));
		}
		
	}
	}
	}
}

kernel void sph_CalcNewSurfaceNormal (
global int* surface_grid_rho,
global float4* surface_normal,
const int gridSize
)
{
	uint id_x = get_global_id(0);
	uint id_y = get_global_id(1);
	uint id_z = get_global_id(2);
	
	float4 normal;
	
	if (id_x != 0 && id_y != 0 && id_z != 0 && 
		id_x != gridSize - 1 && id_y != gridSize - 1 && id_z != gridSize - 1) {
	
		normal.x = (surface_grid_rho[id_x + 1 + gridSize * id_y + gridSize * gridSize * id_z] 
				- surface_grid_rho[id_x - 1 + gridSize * id_y + gridSize * gridSize * id_z]) / (2 * gridSize);	//gridSize ?
		  
		normal.y = (surface_grid_rho[id_x + gridSize * (id_y + 1) + gridSize * gridSize * id_z] 
				- surface_grid_rho[id_x + gridSize * (id_y - 1) + gridSize * gridSize * id_z]) / (2 * gridSize);	//gridSize ?
		  			
		normal.z = (surface_grid_rho[id_x + gridSize * id_y + gridSize * gridSize * (id_z + 1)] 
				- surface_grid_rho[id_x + gridSize * id_y + gridSize * gridSize * (id_z - 1)]) / (2 * gridSize);	//gridSize ?
	}

	surface_normal[id_x + gridSize * id_y + gridSize * gridSize * id_z] = normal / length(normal);
}



//--------------------------------------------------------------
//		map to identify the correct configuration
//		entry 0 of each line: configuration-number
//		entries 1 to 9 of each line: permutation to
//			rotate the configuration appropriately
//--------------------------------------------------------------

constant int case_map[256*9] = {0, 0, 1, 2, 3, 4, 5, 6, 7, 
								1, 0, 1, 2, 3, 4, 5, 6, 7, 
								1, 1, 2, 3, 0, 5, 6, 7, 4, 
								2, 0, 1, 2, 3, 4, 5, 6, 7, 
								1, 2, 3, 0, 1, 6, 7, 4, 5, 
								3, 0, 1, 2, 3, 4, 5, 6, 7, 
								2, 1, 2, 3, 0, 5, 6, 7, 4, 
								5, 3, 2, 6, 7, 0, 1, 5, 4, 
								1, 3, 0, 1, 2, 7, 4, 5, 6, 
								2, 3, 0, 1, 2, 7, 4, 5, 6, 
								3, 3, 0, 1, 2, 7, 4, 5, 6, 
								5, 2, 1, 5, 6, 3, 0, 4, 7, 
								2, 2, 3, 0, 1, 6, 7, 4, 5, 
								5, 1, 0, 4, 5, 2, 3, 7, 6, 
								5, 0, 3, 7, 4, 1, 2, 6, 5, 
								8, 2, 1, 5, 6, 3, 0, 4, 7, 
								1, 4, 7, 6, 5, 0, 3, 2, 1, 
								2, 4, 0, 3, 7, 5, 1, 2, 6, 
								3, 4, 5, 1, 0, 7, 6, 2, 3, 
								5, 5, 4, 7, 6, 1, 0, 3, 2, 
								4, 2, 3, 0, 1, 6, 7, 4, 5, 
								6, 4, 0, 3, 7, 5, 1, 2, 6, 
								6, 2, 1, 5, 6, 3, 0, 4, 7, 
								14, 5, 4, 7, 6, 1, 0, 3, 2, 
								3, 4, 0, 3, 7, 5, 1, 2, 6, 
								5, 7, 3, 2, 6, 4, 0, 1, 5, 
								7, 2, 3, 0, 1, 6, 7, 4, 5, 
								9, 1, 5, 6, 2, 0, 4, 7, 3, 
								6, 2, 3, 0, 1, 6, 7, 4, 5, 
								11, 2, 1, 5, 6, 3, 0, 4, 7, 
								12, 0, 3, 7, 4, 1, 2, 6, 5, 
								5, 4, 5, 1, 0, 7, 6, 2, 3, 
								1, 5, 6, 2, 1, 4, 7, 3, 0, 
								3, 0, 4, 5, 1, 3, 7, 6, 2, 
								2, 1, 5, 6, 2, 0, 4, 7, 3, 
								5, 4, 0, 3, 7, 5, 1, 2, 6, 
								3, 2, 1, 5, 6, 3, 0, 4, 7, 
								7, 3, 0, 1, 2, 7, 4, 5, 6, 
								5, 6, 5, 4, 7, 2, 1, 0, 3, 
								9, 5, 4, 7, 6, 1, 0, 3, 2, 
								4, 3, 0, 1, 2, 7, 4, 5, 6, 
								6, 3, 0, 1, 2, 7, 4, 5, 6, 
								6, 5, 1, 0, 4, 6, 2, 3, 7, 
								11, 5, 4, 7, 6, 1, 0, 3, 2, 
								6, 3, 2, 6, 7, 0, 1, 5, 4, 
								12, 1, 0, 4, 5, 2, 3, 7, 6, 
								14, 6, 5, 4, 7, 2, 1, 0, 3, 
								5, 5, 6, 2, 1, 4, 7, 3, 0, 
								2, 5, 4, 7, 6, 1, 0, 3, 2, 
								5, 1, 5, 6, 2, 0, 4, 7, 3, 
								5, 0, 1, 2, 3, 4, 5, 6, 7, 
								8, 0, 1, 2, 3, 4, 5, 6, 7, 
								6, 4, 5, 1, 0, 7, 6, 2, 3, 
								12, 1, 5, 6, 2, 0, 4, 7, 3, 
								11, 4, 0, 3, 7, 5, 1, 2, 6, 
								5, 2, 3, 0, 1, 6, 7, 4, 5, 
								6, 5, 4, 7, 6, 1, 0, 3, 2, 
								14, 1, 5, 6, 2, 0, 4, 7, 3, 
								12, 0, 1, 2, 3, 4, 5, 6, 7, 
								5, 3, 7, 4, 0, 2, 6, 5, 1, 
								10, 3, 0, 1, 2, 7, 4, 5, 6, 
								16, 7, 6, 5, 4, 3, 2, 1, 0, 
								16, 6, 7, 3, 2, 5, 4, 0, 1, 
								2, 7, 6, 5, 4, 3, 2, 1, 0, 
								1, 6, 2, 1, 5, 7, 3, 0, 4, 
								4, 0, 1, 2, 3, 4, 5, 6, 7, 
								3, 1, 5, 6, 2, 0, 4, 7, 3, 
								6, 0, 1, 2, 3, 4, 5, 6, 7, 
								2, 6, 2, 1, 5, 7, 3, 0, 4, 
								6, 6, 2, 1, 5, 7, 3, 0, 4, 
								5, 5, 1, 0, 4, 6, 2, 3, 7, 
								11, 6, 5, 4, 7, 2, 1, 0, 3, 
								3, 6, 7, 3, 2, 5, 4, 0, 1, 
								6, 0, 3, 7, 4, 1, 2, 6, 5, 
								7, 0, 1, 2, 3, 4, 5, 6, 7, 
								12, 2, 1, 5, 6, 3, 0, 4, 7, 
								5, 7, 6, 5, 4, 3, 2, 1, 0, 
								14, 7, 6, 5, 4, 3, 2, 1, 0, 
								9, 6, 5, 4, 7, 2, 1, 0, 3, 
								5, 6, 7, 3, 2, 5, 4, 0, 1, 
								3, 6, 5, 4, 7, 2, 1, 0, 3, 
								6, 0, 4, 5, 1, 3, 7, 6, 2, 
								7, 2, 1, 5, 6, 3, 0, 4, 7, 
								12, 5, 4, 7, 6, 1, 0, 3, 2, 
								6, 2, 6, 7, 3, 1, 5, 4, 0, 
								10, 2, 1, 5, 6, 3, 0, 4, 7, 
								12, 5, 1, 0, 4, 6, 2, 3, 7, 
								16, 3, 7, 4, 0, 2, 6, 5, 1, 
								7, 5, 4, 7, 6, 1, 0, 3, 2, 
								12, 7, 3, 2, 6, 4, 0, 1, 5, 
								13, 3, 0, 1, 2, 7, 4, 5, 6, 
								17, 1, 5, 6, 2, 0, 4, 7, 3, 
								12, 7, 6, 5, 4, 3, 2, 1, 0, 
								16, 1, 5, 6, 2, 0, 4, 7, 3, 
								17, 6, 5, 4, 7, 2, 1, 0, 3, 
								15, 5, 4, 7, 6, 1, 0, 3, 2, 
								2, 5, 6, 2, 1, 4, 7, 3, 0, 
								6, 6, 5, 4, 7, 2, 1, 0, 3, 
								5, 2, 6, 7, 3, 1, 5, 4, 0, 
								14, 4, 0, 3, 7, 5, 1, 2, 6, 
								5, 1, 2, 3, 0, 5, 6, 7, 4, 
								12, 1, 2, 3, 0, 5, 6, 7, 4, 
								8, 1, 2, 3, 0, 5, 6, 7, 4, 
								5, 0, 4, 5, 1, 3, 7, 6, 2, 
								6, 5, 6, 2, 1, 4, 7, 3, 0, 
								10, 0, 1, 2, 3, 4, 5, 6, 7, 
								12, 2, 6, 7, 3, 1, 5, 4, 0, 
								16, 4, 7, 6, 5, 0, 3, 2, 1, 
								11, 3, 7, 4, 0, 2, 6, 5, 1, 
								16, 7, 4, 0, 3, 6, 5, 1, 2, 
								5, 3, 0, 1, 2, 7, 4, 5, 6, 
								2, 4, 7, 6, 5, 0, 3, 2, 1, 
								5, 7, 4, 0, 3, 6, 5, 1, 2, 
								11, 0, 1, 2, 3, 4, 5, 6, 7, 
								9, 1, 2, 3, 0, 5, 6, 7, 4, 
								5, 6, 2, 1, 5, 7, 3, 0, 4, 
								14, 1, 2, 3, 0, 5, 6, 7, 4, 
								16, 7, 3, 2, 6, 4, 0, 1, 5, 
								5, 4, 7, 6, 5, 0, 3, 2, 1, 
								2, 3, 7, 4, 0, 2, 6, 5, 1, 
								12, 7, 4, 0, 3, 6, 5, 1, 2, 
								16, 1, 2, 3, 0, 5, 6, 7, 4, 
								17, 1, 2, 3, 0, 5, 6, 7, 4, 
								15, 2, 6, 7, 3, 1, 5, 4, 0, 
								16, 1, 0, 4, 5, 2, 3, 7, 6, 
								4, 1, 2, 3, 0, 5, 6, 7, 4, 
								15, 7, 4, 0, 3, 6, 5, 1, 2, 
								1, 7, 6, 5, 4, 3, 2, 1, 0, 
								1, 7, 6, 5, 4, 3, 2, 1, 0, 
								3, 7, 4, 0, 3, 6, 5, 1, 2, 
								4, 1, 2, 3, 0, 5, 6, 7, 4, 
								6, 1, 0, 4, 5, 2, 3, 7, 6, 
								3, 2, 6, 7, 3, 1, 5, 4, 0, 
								7, 1, 2, 3, 0, 5, 6, 7, 4, 
								6, 1, 2, 3, 0, 5, 6, 7, 4, 
								12, 3, 2, 6, 7, 0, 1, 5, 4, 
								2, 3, 7, 4, 0, 2, 6, 5, 1, 
								5, 4, 7, 6, 5, 0, 3, 2, 1, 
								6, 7, 3, 2, 6, 4, 0, 1, 5, 
								14, 2, 1, 5, 6, 3, 0, 4, 7, 
								5, 6, 2, 1, 5, 7, 3, 0, 4, 
								9, 2, 1, 5, 6, 3, 0, 4, 7, 
								11, 7, 6, 5, 4, 3, 2, 1, 0, 
								5, 7, 4, 0, 3, 6, 5, 1, 2, 
								2, 4, 7, 6, 5, 0, 3, 2, 1, 
								5, 3, 0, 1, 2, 7, 4, 5, 6, 
								6, 7, 4, 0, 3, 6, 5, 1, 2, 
								11, 1, 5, 6, 2, 0, 4, 7, 3, 
								6, 4, 7, 6, 5, 0, 3, 2, 1, 
								12, 3, 0, 1, 2, 7, 4, 5, 6, 
								10, 2, 3, 0, 1, 6, 7, 4, 5, 
								16, 5, 6, 2, 1, 4, 7, 3, 0, 
								5, 0, 4, 5, 1, 3, 7, 6, 2, 
								8, 3, 0, 1, 2, 7, 4, 5, 6, 
								12, 0, 4, 5, 1, 3, 7, 6, 2, 
								5, 1, 2, 3, 0, 5, 6, 7, 4, 
								14, 6, 2, 1, 5, 7, 3, 0, 4, 
								5, 2, 6, 7, 3, 1, 5, 4, 0, 
								16, 6, 5, 4, 7, 2, 1, 0, 3, 
								2, 5, 6, 2, 1, 4, 7, 3, 0, 
								3, 5, 4, 7, 6, 1, 0, 3, 2, 
								7, 6, 5, 4, 7, 2, 1, 0, 3, 
								6, 1, 5, 6, 2, 0, 4, 7, 3, 
								12, 4, 0, 3, 7, 5, 1, 2, 6, 
								7, 1, 5, 6, 2, 0, 4, 7, 3, 
								13, 0, 1, 2, 3, 4, 5, 6, 7, 
								12, 6, 5, 4, 7, 2, 1, 0, 3, 
								17, 5, 4, 7, 6, 1, 0, 3, 2, 
								6, 3, 7, 4, 0, 2, 6, 5, 1, 
								12, 4, 7, 6, 5, 0, 3, 2, 1, 
								10, 5, 6, 2, 1, 4, 7, 3, 0, 
								16, 2, 6, 7, 3, 1, 5, 4, 0, 
								12, 6, 2, 1, 5, 7, 3, 0, 4, 
								17, 2, 1, 5, 6, 3, 0, 4, 7, 
								16, 0, 4, 5, 1, 3, 7, 6, 2, 
								15, 6, 5, 4, 7, 2, 1, 0, 3, 
								5, 6, 7, 3, 2, 5, 4, 0, 1, 
								9, 0, 1, 2, 3, 4, 5, 6, 7, 
								14, 0, 1, 2, 3, 4, 5, 6, 7, 
								5, 7, 6, 5, 4, 3, 2, 1, 0, 
								12, 6, 7, 3, 2, 5, 4, 0, 1, 
								17, 0, 1, 2, 3, 4, 5, 6, 7, 
								16, 0, 3, 7, 4, 1, 2, 6, 5, 
								15, 6, 7, 3, 2, 5, 4, 0, 1, 
								11, 3, 0, 1, 2, 7, 4, 5, 6, 
								5, 5, 1, 0, 4, 6, 2, 3, 7, 
								16, 6, 2, 1, 5, 7, 3, 0, 4, 
								2, 6, 2, 1, 5, 7, 3, 0, 4, 
								16, 0, 1, 2, 3, 4, 5, 6, 7, 
								15, 1, 5, 6, 2, 0, 4, 7, 3, 
								4, 0, 1, 2, 3, 4, 5, 6, 7, 
								1, 6, 2, 1, 5, 7, 3, 0, 4, 
								2, 7, 6, 5, 4, 3, 2, 1, 0, 
								6, 6, 7, 3, 2, 5, 4, 0, 1, 
								6, 7, 6, 5, 4, 3, 2, 1, 0, 
								10, 1, 2, 3, 0, 5, 6, 7, 4, 
								5, 3, 7, 4, 0, 2, 6, 5, 1, 
								12, 3, 7, 4, 0, 2, 6, 5, 1, 
								14, 3, 7, 4, 0, 2, 6, 5, 1, 
								16, 5, 4, 7, 6, 1, 0, 3, 2, 
								5, 2, 3, 0, 1, 6, 7, 4, 5, 
								11, 6, 2, 1, 5, 7, 3, 0, 4, 
								12, 2, 3, 0, 1, 6, 7, 4, 5, 
								16, 4, 5, 1, 0, 7, 6, 2, 3, 
								8, 2, 3, 0, 1, 6, 7, 4, 5, 
								5, 0, 1, 2, 3, 4, 5, 6, 7, 
								5, 1, 5, 6, 2, 0, 4, 7, 3, 
								2, 5, 4, 7, 6, 1, 0, 3, 2, 
								5, 5, 6, 2, 1, 4, 7, 3, 0, 
								14, 3, 0, 1, 2, 7, 4, 5, 6, 
								12, 5, 6, 2, 1, 4, 7, 3, 0, 
								16, 3, 2, 6, 7, 0, 1, 5, 4, 
								11, 2, 3, 0, 1, 6, 7, 4, 5, 
								16, 5, 1, 0, 4, 6, 2, 3, 7, 
								16, 3, 0, 1, 2, 7, 4, 5, 6, 
								4, 3, 0, 1, 2, 7, 4, 5, 6, 
								9, 3, 0, 1, 2, 7, 4, 5, 6, 
								5, 6, 5, 4, 7, 2, 1, 0, 3, 
								17, 3, 0, 1, 2, 7, 4, 5, 6, 
								15, 2, 1, 5, 6, 3, 0, 4, 7, 
								5, 4, 0, 3, 7, 5, 1, 2, 6, 
								2, 1, 5, 6, 2, 0, 4, 7, 3, 
								15, 0, 4, 5, 1, 3, 7, 6, 2, 
								1, 5, 6, 2, 1, 4, 7, 3, 0, 
								5, 4, 5, 1, 0, 7, 6, 2, 3, 
								12, 4, 5, 1, 0, 7, 6, 2, 3, 
								11, 1, 2, 3, 0, 5, 6, 7, 4, 
								16, 2, 3, 0, 1, 6, 7, 4, 5, 
								9, 2, 3, 0, 1, 6, 7, 4, 5, 
								17, 2, 3, 0, 1, 6, 7, 4, 5, 
								5, 7, 3, 2, 6, 4, 0, 1, 5, 
								15, 4, 0, 3, 7, 5, 1, 2, 6, 
								14, 2, 3, 0, 1, 6, 7, 4, 5, 
								16, 2, 1, 5, 6, 3, 0, 4, 7, 
								16, 4, 0, 3, 7, 5, 1, 2, 6, 
								4, 2, 3, 0, 1, 6, 7, 4, 5, 
								5, 5, 4, 7, 6, 1, 0, 3, 2, 
								15, 4, 5, 1, 0, 7, 6, 2, 3, 
								2, 4, 0, 3, 7, 5, 1, 2, 6, 
								1, 4, 7, 6, 5, 0, 3, 2, 1, 
								8, 5, 6, 2, 1, 4, 7, 3, 0, 
								5, 0, 3, 7, 4, 1, 2, 6, 5, 
								5, 1, 0, 4, 5, 2, 3, 7, 6, 
								2, 2, 3, 0, 1, 6, 7, 4, 5, 
								5, 2, 1, 5, 6, 3, 0, 4, 7, 
								15, 3, 0, 1, 2, 7, 4, 5, 6, 
								2, 3, 0, 1, 2, 7, 4, 5, 6, 
								1, 3, 0, 1, 2, 7, 4, 5, 6, 
								5, 3, 2, 6, 7, 0, 1, 5, 4, 
								2, 1, 2, 3, 0, 5, 6, 7, 4, 
								15, 0, 1, 2, 3, 4, 5, 6, 7, 
								1, 2, 3, 0, 1, 6, 7, 4, 5, 
								2, 0, 1, 2, 3, 4, 5, 6, 7, 
								1, 1, 2, 3, 0, 5, 6, 7, 4, 
								1, 0, 1, 2, 3, 4, 5, 6, 7, 
								0, 0, 1, 2, 3, 4, 5, 6, 7};


constant int3 verts[8] = {(int3)(0,0,0),
					   (int3)(1,0,0),
					   (int3)(1,1,0),
					   (int3)(0,1,0),
					   (int3)(0,0,1),
					   (int3)(1,0,1),
					   (int3)(1,1,1),
					   (int3)(0,1,1)};

constant int edgesToVerts[24] = {1,2,
								2,3,
								3,4,
								4,1,
								5,6,
								6,7,
								7,8,
								8,5,
								1,5,
								2,6,
								4,8,
								3,7};


//--------------------------------------------------------
//		maps to build the configurations
//--------------------------------------------------------

constant int confVertex_id[] = {0,0,3,7,13,19,24,31,40,44,50,58,64,72,84,90,96,103,112};									
constant int confIndex_id[] = {0,0,3,9,15,21,30,39,48,54,66,78,90,102,114,126,138,153,168};

constant int confVertex[] = {0,3,8,							//1
							8,9,1,3,						//2
							0,3,8,1,11,2,					//3
							0,3,8,11,5,6,					//4
							0,1,8,7,5,						//5
							8,9,1,3,11,5,6,					//6
							3,2,10,11,5,6,0,9,1,			//7
							3,1,5,7,						//8
							3,10,6,0,5,9,					//9
							0,2,8,10,4,6,11,9,				//10
							0,3,7,11,6,9,					//11
							0,1,8,7,5,3,2,10,				//12
							0,8,3,9,5,4,1,2,11,7,6,10,		//13
							0,1,5,10,8,6,					//14
							
							// additional cases to cope with ambiguity (reduce number of holes in surface)
							8,2,3,11,1,0,					
							9,5,8,3,1,11,6,
							3,10,6,0,5,9,1,2,11,
							};
  constant int confIndex[] = {2,1,0, 
							  2,1,0,3,2,0,
							  2,1,0,5,4,3,					//3
							  2,1,0,5,4,3,					//4
							  0,1,2,2,1,3,1,4,3,
							  2,1,0,3,2,0,6,5,4,
							  2,1,0,5,4,3,8,7,6,
							  0,1,2,0,2,3,					//8
							  2,1,0,3,2,0,4,2,3,5,4,3,
							  2,1,0,2,3,1,6,5,4,6,4,7,
							  2,1,0,3,2,0,3,4,2,3,0,5,
							  0,1,2,2,1,3,1,4,3,7,6,5,
							  0,1,2,3,4,5,6,7,8,9,10,11,			
							  0,1,2,0,2,3,0,3,4,3,2,5,
			
							// additional cases to cope with ambiguity (reduce number of holes in surface)
							  0,1,2,0,3,1,0,4,3,0,5,4,
							  0,1,2,2,1,3,4,3,5,3,6,5,1,6,3,
							  2,1,0,3,2,0,4,2,3,5,4,3,6,7,8						
							};



kernel void sph_CalcNewSurface2(
global float* surface_Pos,
global int* surface_Ind,
const int gridSize,
global int* surface_grid_rho,
global int* COUNT,
const float m
)
{
	float4 tw = (float4)(0.0125,0,0,0);
	int t = (int)(W2(&tw, 0.15) * m * 10000000000);
	//int t = (int)(W(&tw) * m * 10000000000);
	int Cnt;
	
	int id_x = get_global_id(0);
	int id_y = get_global_id(1);
	int id_z = get_global_id(2);

	int rho[8] = {};
	for (int i = 0; i < 8; i++) {
		
		if (id_x + verts[i].x == 0 || id_y + verts[i].y == 0 || id_z + verts[i].z == 0
			|| id_x + verts[i].x == gridSize || id_y + verts[i].y == gridSize || id_z + verts[i].z == gridSize) {
			
			// zero border density
			rho[i] = 0;
		}
		else {
			rho[i] = surface_grid_rho[id_x + verts[i].x + gridSize * (id_y + verts[i].y) + gridSize * gridSize * (id_z + verts[i].z)];
		}
	}

	//--------------------------------------
	//		calculate code
	//--------------------------------------

	int c = 0;
	int cnt = 0;
	int power = 1;
	
	for (int i = 0; i < 8; i++) {
	
		if (rho[i] > t) {
			c += power;
			cnt++;
		}
		power *= 2;
	}

	//--------------------------------------
	//		calculate edges
	//--------------------------------------

	float3 edges[12] = {
						/*(float3) (0, -1, -1),
						(float3) (1,  0, -1),
						(float3) (0,  1, -1),
						(float3) (-1, 0, -1),
						(float3) (0, -1,  1),
						(float3) (1,  0,  1),
						(float3) (0,  1,  1),
						(float3) (-1, 0,  1),
						(float3) (-1, -1, 0),
						(float3) ( 1, -1, 0),
						(float3) (-1,  1, 0),
						(float3) ( 1,  1, 0),*/
						};
	float3 pos = (float3)(id_x, id_y, id_z);
	float3 one = (float3)1;
	
	
	for (int i = 0; i < 12; i++) {
	
		int id1 = case_map[ 9 * c + edgesToVerts[2 * i]];
		int id2 = case_map[ 9 * c + edgesToVerts[2 * i + 1]];
			
		float3 vert1 = convert_float3(verts[id1]);
		float3 vert2 = convert_float3(verts[id2]);
		
		float s = (float)(t - rho[id1]) / (float)(rho[id2] - rho[id1]);
		float3 tmp = (1-s) * vert1 + s * vert2;
		
		edges[i] = 2 * (tmp + pos) / gridSize - one;
	 
	}
	
	//--------------------------------------
	//		set Vertex- and IndexBuffer
	//--------------------------------------
	
	
	if (case_map[9 * c] != 0) {
		
		int i = case_map[9 * c];
		
		Cnt = atomic_inc(COUNT);
		
		for (int j = confVertex_id[i]; j < confVertex_id[i + 1]; j++) {
			
			int l = j - confVertex_id[i];
			
			surface_Pos[3 * (12 * Cnt + l)] = edges[confVertex[j]].x; 
			surface_Pos[3 * (12 * Cnt + l) + 1] = edges[confVertex[j]].y;
			surface_Pos[3 * (12 * Cnt + l) + 2] = edges[confVertex[j]].z;
		}

		if(cnt <= 4 || i > 14)
		{
			for (int j = confIndex_id[i]; j < confIndex_id[i + 1]; j++) {
				int l = j - confIndex_id[i];
				surface_Ind[15 * Cnt + l] = 12 * Cnt + confIndex[j];
			}
		}
		else 
		{
			int a = 0;
			for (int j = confIndex_id[i + 1] -1; j >= confIndex_id[i]; j--) {
				int l = j - confIndex_id[i];
				surface_Ind[15 * Cnt + a] =  12 * Cnt + confIndex[j];
				a++;
			}
		}

	}
}




kernel void sph_resetSurfaceInd(
global int* surfaceInd
)
{
	uint id = get_global_id(0);
	surfaceInd[id] = -1;
}

