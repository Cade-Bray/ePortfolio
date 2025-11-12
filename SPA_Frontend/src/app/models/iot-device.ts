export interface IoT {
  _id: string, // Mongodb Object ID
  name: string,
  temp: string,
  lastChecked: Date,
  status: string,
  location: string
}
