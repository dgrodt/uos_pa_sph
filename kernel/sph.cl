#define BUFFER_SIZE_SIDE 128
#define BUFFER_SIZE_DEPTH 32
#define OFFSET 5
#define H 0.2

float W (float4* r) {
	float x = fast_length(*r);
	float k = 315 / (64 * 3.14159 * H*H*H*H*H*H*H*H*H); //float k = 3059924.7;
	if (x > H) return 0;
	return k * (H*H-x*x)*(H*H-x*x)*(H*H-x*x);
}
float4 gradW (float4* r) {

	float x = fast_length(*r);
	float k = 45 / (3.14159 * H*H*H*H*H*H); //float k = 223811.6;
	if (x > H) return 0;
	return k * ((H-x)*(H-x)*(H-x)) * (*r) / x;
}
float4 gradWV (float4* r) {
	float x = fast_length(*r);
	float k = 45 / (3.14159 * H*H*H*H*H*H); //float k = 223811.6;
	if (x > H) return 0;
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
	
	float nu = 0.000001;
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
	if(r < 0.05){ a_W += (0.05 - r)* (pos - right)/ (fast_length(pos - right)* pown(DELTA_T,2)); }

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
    int cnt = atomic_inc(&data[cnt_ind]) + 1;
    if(cnt > BUFFER_SIZE_DEPTH -2)
    	return;
    data[cnt_ind + cnt] = id;
    
}