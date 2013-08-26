#define BUFFER_SIZE_SIDE 20
#define BUFFER_SIZE_DEPTH 50
#define OFFSET 1

float W (float4 r, float h) {

	float x = length(r);
	//float k = 315 / (64 * 3.14159 * pown(h,9));
	float k = 3059924.7;
	
	if (x > h) return 0;
	return k * pown(h*h-x*x,3);
}


float4 gradW (float4 r, float h) {

	float x = length(r);
	//float k = 45 / (3.14159 * pown(h,6));
	float k = 223811.6;
	
	if (x > h) return 0;
	return k * pown(h-x,3) * r / x;
}

float4 gradWV (float4 r, float h) {

	float x = length(r);
	//float k = 45 / (3.14159 * pown(h,6));
	float k = 223811.6;
	
	if (x > h) return 0;
	return k * (h-x);
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
	float h = 0.2;

	float rhoByM = 0;
	float4 pos = body_Pos[id];
	
	//for (int i = 0; i < N; i++) {
	int4 gridPos = convert_int4((BUFFER_SIZE_SIDE - 1) * (body_Pos[id] + (float4)1) / 2);
	
	for (int l = max(gridPos.x - OFFSET, 0); l <= min(gridPos.x + OFFSET, BUFFER_SIZE_SIDE - 1) ; l++) {
	for (int j = max(gridPos.y - OFFSET, 0); j <= min(gridPos.y + OFFSET, BUFFER_SIZE_SIDE - 1) ; j++) {
	for (int k = max(gridPos.z - OFFSET, 0); k <= min(gridPos.z + OFFSET, BUFFER_SIZE_SIDE - 1) ; k++) {

	 	int cnt_ind = BUFFER_SIZE_DEPTH * (l + BUFFER_SIZE_SIDE * j + BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * k);
		uint cnt = data[cnt_ind];
		for (int o = 1; o <= cnt; o++) {
		
			int i = data[cnt_ind + o];
			rhoByM += W(pos-body_Pos[i], h);
			
		}
	}
	}
	}
	
	body_rho[id] = m * rhoByM;
}


kernel void sph_CalcNewP(
global float* body_P,
global float* body_rho,
const float rho
)
{
	uint id = get_global_id(0);
	body_P[id] = (pown(body_rho[id]/rho, 7) -1) /300;
	//body_P[id] = 0 + (body_rho[id] - rho) * 20;
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
	float h = 0.2;
	float4 new_n = (float4)0;
	float4 pos = body_Pos[id];
	
	int4 gridPos = convert_int4((BUFFER_SIZE_SIDE - 1) * (body_Pos[id] + (float4)1) / 2);
	
	for (int l = max(gridPos.x - OFFSET, 0); l <= min(gridPos.x + OFFSET, BUFFER_SIZE_SIDE - 1) ; l++) {
	for (int j = max(gridPos.y - OFFSET, 0); j <= min(gridPos.y + OFFSET, BUFFER_SIZE_SIDE - 1) ; j++) {
	for (int k = max(gridPos.z - OFFSET, 0); k <= min(gridPos.z + OFFSET, BUFFER_SIZE_SIDE - 1) ; k++) {

	 	int cnt_ind = BUFFER_SIZE_DEPTH * (l + BUFFER_SIZE_SIDE * j + BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * k);
		uint cnt = data[cnt_ind];
		for (int o = 1; o <= cnt; o++) {

			int i = data[cnt_ind + o];	
	
			if (i!=id)
				new_n += gradW(pos-body_Pos[i], h) / (body_rho[i] * 300000);
		}
	}
	}
	}
	
	//if (length(new_n) > 10) {
		n[id] = new_n / length(new_n);
		n[id].w = length(new_n);
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
	
	float h = 0.2;
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
	
	for (int l = max(gridPos.x - OFFSET, 0); l <= min(gridPos.x + OFFSET, BUFFER_SIZE_SIDE - 1) ; l++) {
	for (int j = max(gridPos.y - OFFSET, 0); j <= min(gridPos.y + OFFSET, BUFFER_SIZE_SIDE - 1) ; j++) {
	for (int k = max(gridPos.z - OFFSET, 0); k <= min(gridPos.z + OFFSET, BUFFER_SIZE_SIDE - 1) ; k++) {

	 	int cnt_ind = BUFFER_SIZE_DEPTH * (l + BUFFER_SIZE_SIDE * j + BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * k);
		uint cnt = data[cnt_ind];
		for (int o = 1; o <= cnt; o++) {
		
			int i = data[cnt_ind + o];
		
			float4 r = pos-body_Pos[i];
			
			a_V += -nu * (V - body_V[i]) * gradWV(r, h) / body_rho[i];
			
			float4 grad = gradW(r, h);
			
			float C = (body_P[i] + P)/(2 * body_rho[i]);
			
			if (id!=i) {
				a_P += C * grad;
			}
		}
	}
	}
	}
	
	//---------------------------------------------------
	//		calculate boundary forces
	//---------------------------------------------------
	
	float r[6];
	float4 b[6];
	b[0] = (float4)(pos.x, -1, pos.z, 0);
	b[1] = (float4)(pos.x, pos.y, -1, 0);
	b[2] = (float4)(pos.x, pos.y, 1, 0);
	b[3] = (float4)(-1, pos.y, pos.z, 0);
	b[4] = (float4)(1, pos.y, pos.z, 0);
	b[5] = (float4)(pos.x, 1, pos.z, 0);
	
	for (int i = 0; i < 6; i++) {
		r[i] = distance(pos, b[i]);

		if (r[i] < 0.05) {
			a_W += (0.05 - r[i]) * (pos - b[i]) / (length(pos - b[i]) * pown(DELTA_T,2));
		}
	
	}
	
	//---------------------------------------------------
	//		calculate tension forces
	//---------------------------------------------------
	
	/*
	float nabla_n = 0;
	
	for (int i = 0; i < N; i++) {

		float4 r = body_Pos[id]-body_Pos[i];
		
		if (length(n[i]) > 0) {
			if (i!=id)
				nabla_n +=  dot(n[i]/length(n[i]), gradW(r, 0.2)) / body_rho[i];
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
    body_Pos[id] += body_V[id] * DELTA_T;
    
    int4 gridPos = convert_int4((BUFFER_SIZE_SIDE - 1) * (body_Pos[id] + (float4)1) / 2);
    int cnt_ind = BUFFER_SIZE_DEPTH * (gridPos.x + BUFFER_SIZE_SIDE * gridPos.y + BUFFER_SIZE_SIDE * BUFFER_SIZE_SIDE * gridPos.z);
    int cnt = atomic_inc(&data[cnt_ind]) + 1;
    data[cnt_ind + cnt] = id;
    
    
}